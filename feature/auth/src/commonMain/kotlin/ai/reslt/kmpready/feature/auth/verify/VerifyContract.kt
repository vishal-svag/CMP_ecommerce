package ai.reslt.kmpready.feature.auth.verify

import ai.reslt.kmpready.core.common.biometric.BiometricKind
import ai.reslt.kmpready.core.ui.UiText
import androidx.compose.runtime.Immutable

@Immutable
data class VerifyState(
    val displayName: String = "",
    val memberReference: String = "",
    val codeLength: Int = 6,
    val code: String = "",
    /** Null = no enrolled biometrics → the biometric card is hidden. */
    val biometricKind: BiometricKind? = null,
    val isScanning: Boolean = false,
    val resendSecondsRemaining: Int = 0,
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    /** Any verification / biometric / resend error — always rendered under the passcode boxes. */
    val codeError: UiText? = null,
    /** Attempt limit reached — the only way forward is back to sign-in. */
    val isLocked: Boolean = false,
    val canCallStylist: Boolean = false,
    val isHelpVisible: Boolean = false,
) {
    val isBusy: Boolean get() = isVerifying || isScanning
    /** A rejected code must be edited before resubmitting (don't burn an attempt on the same digits). */
    val canSubmit: Boolean get() = !isBusy && !isLocked && codeError == null && code.length == codeLength
    val canResend: Boolean get() = !isLocked && !isResending && resendSecondsRemaining == 0
}

sealed interface VerifyIntent {
    data class CodeChanged(val value: String) : VerifyIntent
    data object Submit : VerifyIntent
    data object Scan : VerifyIntent
    data object Resend : VerifyIntent
    data object AlternateDevice : VerifyIntent
    data object CallStylist : VerifyIntent
    /** ✕, system back, or "Back to sign in". */
    data object Exit : VerifyIntent
    data object ShowHelp : VerifyIntent
    data object DismissHelp : VerifyIntent
}

sealed interface VerifyEffect {
    data object NavigateBack : VerifyEffect
    data class ShowMessage(val text: UiText) : VerifyEffect
    data class OpenUri(val uri: String) : VerifyEffect
}
