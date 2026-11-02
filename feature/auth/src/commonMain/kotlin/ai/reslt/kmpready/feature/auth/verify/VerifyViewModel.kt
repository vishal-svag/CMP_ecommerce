package ai.reslt.kmpready.feature.auth.verify

import ai.reslt.kmpready.core.common.config.AppConfig
import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.logging.AppLogger
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.common.time.AppClock
import ai.reslt.kmpready.core.common.biometric.BiometricAuthenticator
import ai.reslt.kmpready.core.common.biometric.BiometricAvailability
import ai.reslt.kmpready.core.common.biometric.BiometricRequest
import ai.reslt.kmpready.core.common.biometric.BiometricResult
import ai.reslt.kmpready.core.domain.model.LoginChallenge
import ai.reslt.kmpready.core.domain.model.VerificationFactor
import ai.reslt.kmpready.core.domain.usecase.CancelLoginChallengeUseCase
import ai.reslt.kmpready.core.domain.usecase.ObservePendingChallengeUseCase
import ai.reslt.kmpready.core.domain.usecase.ResendCodeUseCase
import ai.reslt.kmpready.core.domain.usecase.VerifyLoginUseCase
import ai.reslt.kmpready.core.ui.MviViewModel
import ai.reslt.kmpready.core.ui.UiText
import ai.reslt.kmpready.core.ui.toUiText
import ai.reslt.kmpready.feature.auth.resources.Res
import ai.reslt.kmpready.feature.auth.resources.verify_alternate_device_unavailable
import ai.reslt.kmpready.feature.auth.resources.verify_biometric_failed
import ai.reslt.kmpready.feature.auth.resources.verify_prompt_cancel
import ai.reslt.kmpready.feature.auth.resources.verify_prompt_subtitle
import ai.reslt.kmpready.feature.auth.resources.verify_prompt_title
import ai.reslt.kmpready.feature.auth.resources.verify_resent
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

class VerifyViewModel(
    observeChallenge: ObservePendingChallengeUseCase,
    private val verify: VerifyLoginUseCase,
    private val resend: ResendCodeUseCase,
    private val cancelChallenge: CancelLoginChallengeUseCase,
    private val biometrics: BiometricAuthenticator,
    private val clock: AppClock,
    private val config: AppConfig,
) : MviViewModel<VerifyState, VerifyIntent, VerifyEffect>(VerifyState()) {

    private val log = AppLogger.withTag("Verify")
    private var job: Job? = null
    private var countdown: Job? = null
    private var completed = false

    init {
        val challenge = observeChallenge().value
        if (challenge == null) {
            // e.g. process death between steps — nothing to verify.
            sendEffect(VerifyEffect.NavigateBack)
        } else {
            setState {
                copy(
                    displayName = challenge.displayName,
                    memberReference = challenge.memberReference,
                    codeLength = challenge.codeLength,
                    biometricKind = (biometrics.availability() as? BiometricAvailability.Available)?.kind,
                    canCallStylist = config.conciergePhoneUri != null,
                )
            }
            startCountdown(challenge)
        }
    }

    override fun onIntent(intent: VerifyIntent) {
        when (intent) {
            is VerifyIntent.CodeChanged -> onCodeChanged(intent.value)
            VerifyIntent.Submit -> if (currentState.isLocked) exit() else submit(VerificationFactor.Passcode(currentState.code))
            VerifyIntent.Scan -> scan()
            VerifyIntent.Resend -> resendCode()
            VerifyIntent.AlternateDevice ->
                sendEffect(VerifyEffect.ShowMessage(UiText.Resource(Res.string.verify_alternate_device_unavailable)))
            VerifyIntent.CallStylist -> config.conciergePhoneUri?.let { sendEffect(VerifyEffect.OpenUri(it)) }
            VerifyIntent.Exit -> exit()
            VerifyIntent.ShowHelp -> setState { copy(isHelpVisible = true) }
            VerifyIntent.DismissHelp -> setState { copy(isHelpVisible = false) }
        }
    }

    private fun onCodeChanged(value: String) {
        if (currentState.isBusy) return // readOnly while verifying; ignore stray IME events
        val code = value.filter(Char::isDigit).take(currentState.codeLength)
        setState { copy(code = code, codeError = null) }
        if (code.length == currentState.codeLength && !currentState.isBusy && !currentState.isLocked) {
            submit(VerificationFactor.Passcode(code)) // auto-submit on the last digit
        }
    }

    private fun submit(factor: VerificationFactor) {
        if (job?.isActive == true || currentState.isLocked) return
        job = viewModelScope.launch {
            setState { copy(isVerifying = true, codeError = null) }
            val result = verify(factor)
            setState { copy(isVerifying = false, isScanning = false) }
            when (result) {
                is AppResult.Success -> {
                    // Session is now LoggedIn; the app's auth gate swaps to the main graph.
                    completed = true
                    countdown?.cancel()
                }
                is AppResult.Failure -> handleFailure(result.error, factor)
            }
        }
    }

    /**
     * The entered digits are KEPT on failure (shown in the error state) so the user can see what
     * they typed; the next keystroke starts a fresh code. Every error is surfaced right under the
     * passcode boxes, never only at the bottom of the screen.
     */
    private fun handleFailure(error: AppError, factor: VerificationFactor) {
        log.d { "Verification failed: ${error::class.simpleName} (factor=${factor::class.simpleName})" }
        when (error) {
            AppError.VerificationLocked, AppError.NoPendingVerification -> {
                countdown?.cancel()
                setState { copy(isLocked = true, resendSecondsRemaining = 0, codeError = error.toUiText()) }
            }
            else -> setState { copy(codeError = error.toUiText()) }
        }
    }

    private fun scan() {
        if (job?.isActive == true || currentState.isLocked) return
        job = viewModelScope.launch {
            setState { copy(isScanning = true, codeError = null) }
            val request = BiometricRequest(
                title = getString(Res.string.verify_prompt_title),
                subtitle = getString(Res.string.verify_prompt_subtitle),
                cancelLabel = getString(Res.string.verify_prompt_cancel),
            )
            when (biometrics.authenticate(request)) {
                BiometricResult.Success -> {
                    setState { copy(isScanning = false) }
                    job = null
                    submit(VerificationFactor.DeviceBiometric)
                }
                BiometricResult.Cancelled -> setState { copy(isScanning = false) }
                BiometricResult.Unavailable, is BiometricResult.Failed -> setState {
                    copy(isScanning = false, codeError = UiText.Resource(Res.string.verify_biometric_failed))
                }
            }
        }
    }

    private fun resendCode() {
        if (!currentState.canResend) return
        viewModelScope.launch {
            setState { copy(isResending = true, codeError = null) }
            when (val result = resend()) {
                is AppResult.Success -> {
                    setState { copy(isResending = false, code = "") }
                    startCountdown(result.data)
                    sendEffect(VerifyEffect.ShowMessage(UiText.Resource(Res.string.verify_resent)))
                }
                is AppResult.Failure -> {
                    setState { copy(isResending = false) }
                    handleFailure(result.error, VerificationFactor.DeviceBiometric)
                }
            }
        }
    }

    private fun startCountdown(challenge: LoginChallenge) {
        countdown?.cancel()
        countdown = viewModelScope.launch {
            while (isActive) {
                val remainingMs = challenge.resendAvailableAtMillis - clock.nowMillis()
                val seconds = ((remainingMs + 999) / 1000).coerceAtLeast(0).toInt()
                setState { copy(resendSecondsRemaining = seconds) }
                if (seconds == 0) break
                delay(1_000)
            }
        }
    }

    private fun exit() {
        cancelChallenge()
        sendEffect(VerifyEffect.NavigateBack)
    }

    override fun onCleared() {
        // Leaving step 2 any way other than success must drop the held credentials.
        if (!completed) cancelChallenge()
        super.onCleared()
    }
}
