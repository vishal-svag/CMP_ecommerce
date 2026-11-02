package ai.reslt.kmpready.feature.auth.login

import ai.reslt.kmpready.core.common.biometric.BiometricKind
import ai.reslt.kmpready.core.domain.model.IdentifierType
import ai.reslt.kmpready.core.ui.UiText
import androidx.compose.runtime.Immutable

@Immutable
data class LoginState(
    val identifierType: IdentifierType = IdentifierType.ClientIdOrEmail,
    val identifier: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val rememberMe: Boolean = false,
    /** Non-null when the device has enrolled biometrics (drives the "enabled" caption). */
    val biometricKind: BiometricKind? = null,
    val showConciergeDesk: Boolean = true,
    val identifierError: UiText? = null,
    val passwordError: UiText? = null,
    val formError: UiText? = null,
    val isLoading: Boolean = false,
    val isHelpVisible: Boolean = false,
) {
    val canSubmit: Boolean get() = !isLoading && identifier.isNotBlank() && password.isNotBlank()
}

sealed interface LoginIntent {
    data class IdentifierTypeChanged(val type: IdentifierType) : LoginIntent
    data class IdentifierChanged(val value: String) : LoginIntent
    data class PasswordChanged(val value: String) : LoginIntent
    data object TogglePasswordVisibility : LoginIntent
    data class RememberMeChanged(val value: Boolean) : LoginIntent
    data object Submit : LoginIntent
    data object SignInWithApple : LoginIntent
    data object SignInWithPasskey : LoginIntent
    data object ForgotKey : LoginIntent
    data object ContactConcierge : LoginIntent
    data object ShowHelp : LoginIntent
    data object DismissHelp : LoginIntent
}

sealed interface LoginEffect {
    data object NavigateToVerification : LoginEffect
    data class ShowMessage(val text: UiText) : LoginEffect
    data class OpenUri(val uri: String) : LoginEffect
}
