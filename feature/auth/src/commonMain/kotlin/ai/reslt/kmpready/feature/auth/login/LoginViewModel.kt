package ai.reslt.kmpready.feature.auth.login

import ai.reslt.kmpready.core.common.biometric.BiometricAuthenticator
import ai.reslt.kmpready.core.common.biometric.BiometricAvailability
import ai.reslt.kmpready.core.common.config.AppConfig
import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.error.AppError.Validation.Field
import ai.reslt.kmpready.core.common.error.AppError.Validation.Reason
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.domain.model.Credentials
import ai.reslt.kmpready.core.domain.model.ExternalSignInMethod
import ai.reslt.kmpready.core.domain.usecase.ExternalSignInUseCase
import ai.reslt.kmpready.core.domain.usecase.LoginUseCase
import ai.reslt.kmpready.core.ui.MviViewModel
import ai.reslt.kmpready.core.ui.UiText
import ai.reslt.kmpready.core.ui.toUiText
import ai.reslt.kmpready.feature.auth.resources.Res
import ai.reslt.kmpready.feature.auth.resources.login_forgot_key_message
import ai.reslt.kmpready.feature.auth.resources.login_member_invalid
import ai.reslt.kmpready.feature.auth.resources.login_password_too_short
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LoginViewModel(
    private val login: LoginUseCase,
    private val externalSignIn: ExternalSignInUseCase,
    biometrics: BiometricAuthenticator,
    private val config: AppConfig,
) : MviViewModel<LoginState, LoginIntent, LoginEffect>(
    LoginState(
        biometricKind = (biometrics.availability() as? BiometricAvailability.Available)?.kind,
        showConciergeDesk = config.conciergeEmailUri != null,
    ),
) {
    private var job: Job? = null

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.IdentifierTypeChanged -> setState {
                if (identifierType == intent.type) this
                else copy(identifierType = intent.type, identifier = "", identifierError = null, formError = null)
            }
            is LoginIntent.IdentifierChanged -> setState { copy(identifier = intent.value, identifierError = null, formError = null) }
            is LoginIntent.PasswordChanged -> setState { copy(password = intent.value, passwordError = null, formError = null) }
            LoginIntent.TogglePasswordVisibility -> setState { copy(isPasswordVisible = !isPasswordVisible) }
            is LoginIntent.RememberMeChanged -> setState { copy(rememberMe = intent.value) }
            LoginIntent.Submit -> submit()
            LoginIntent.SignInWithApple -> external(ExternalSignInMethod.Apple)
            LoginIntent.SignInWithPasskey -> external(ExternalSignInMethod.Passkey)
            LoginIntent.ForgotKey -> sendEffect(LoginEffect.ShowMessage(UiText.Resource(Res.string.login_forgot_key_message)))
            LoginIntent.ContactConcierge -> config.conciergeEmailUri?.let { sendEffect(LoginEffect.OpenUri(it)) }
            LoginIntent.ShowHelp -> setState { copy(isHelpVisible = true) }
            LoginIntent.DismissHelp -> setState { copy(isHelpVisible = false) }
        }
    }

    private fun credentials() = with(currentState) {
        Credentials(identifier = identifier, password = password, identifierType = identifierType, rememberMe = rememberMe)
    }

    private fun submit() {
        if (job?.isActive == true) return // ignore double taps
        val credentials = credentials()
        login.validate(credentials)?.let { applyValidationError(it); return }

        job = viewModelScope.launch {
            setState { copy(isLoading = true, formError = null) }
            when (val result = login(credentials)) {
                is AppResult.Success -> {
                    // Step 1 done: the password is no longer needed in memory.
                    setState { copy(isLoading = false, password = "", isPasswordVisible = false) }
                    sendEffect(LoginEffect.NavigateToVerification)
                }
                is AppResult.Failure -> {
                    setState { copy(isLoading = false) }
                    val error = result.error
                    if (error is AppError.Validation) applyValidationError(error)
                    else setState { copy(formError = error.toUiText()) }
                }
            }
        }
    }

    private fun external(method: ExternalSignInMethod) {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            val result = externalSignIn(method)
            if (result is AppResult.Failure) sendEffect(LoginEffect.ShowMessage(result.error.toUiText()))
            // Success flips the session; the app gate navigates.
        }
    }

    private fun applyValidationError(error: AppError.Validation) {
        val message = when {
            error.field == Field.Password && error.reason == Reason.TooShort ->
                UiText.Resource(Res.string.login_password_too_short, listOf(LoginUseCase.MIN_PASSWORD_LENGTH))
            error.field == Field.Identifier && error.reason == Reason.Invalid ->
                UiText.Resource(Res.string.login_member_invalid)
            else -> error.toUiText()
        }
        setState {
            when (error.field) {
                Field.Identifier -> copy(identifierError = message)
                Field.Password, Field.Code -> copy(passwordError = message)
            }
        }
    }
}
