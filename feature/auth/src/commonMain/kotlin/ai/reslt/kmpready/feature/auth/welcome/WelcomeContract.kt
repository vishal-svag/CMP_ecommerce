package ai.reslt.kmpready.feature.auth.welcome

import androidx.compose.runtime.Immutable

@Immutable
data class WelcomeState(
    val isHelpVisible: Boolean = false,
)

sealed interface WelcomeIntent {
    data object SignIn : WelcomeIntent
    data object Register : WelcomeIntent
    data object ContinueAsGuest : WelcomeIntent
    /** Header ✕ — dismisses the membership gate, i.e. browse as guest. */
    data object Close : WelcomeIntent
    data object ShowHelp : WelcomeIntent
    data object DismissHelp : WelcomeIntent
}

sealed interface WelcomeEffect {
    data object NavigateToSignIn : WelcomeEffect
    data object NavigateToRegister : WelcomeEffect
}
