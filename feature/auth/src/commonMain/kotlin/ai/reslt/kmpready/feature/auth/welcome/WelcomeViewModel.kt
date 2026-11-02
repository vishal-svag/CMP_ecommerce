package ai.reslt.kmpready.feature.auth.welcome

import ai.reslt.kmpready.core.domain.usecase.ContinueAsGuestUseCase
import ai.reslt.kmpready.core.ui.MviViewModel

/**
 * Navigation to sign-in / register is an effect (owned by the nav graph). Entering guest mode
 * is a session change — the root auth gate swaps graphs, so no navigation call is needed.
 */
class WelcomeViewModel(
    private val continueAsGuest: ContinueAsGuestUseCase,
) : MviViewModel<WelcomeState, WelcomeIntent, WelcomeEffect>(WelcomeState()) {

    override fun onIntent(intent: WelcomeIntent) {
        when (intent) {
            WelcomeIntent.SignIn -> sendEffect(WelcomeEffect.NavigateToSignIn)
            WelcomeIntent.Register -> sendEffect(WelcomeEffect.NavigateToRegister)
            WelcomeIntent.ContinueAsGuest, WelcomeIntent.Close -> continueAsGuest()
            WelcomeIntent.ShowHelp -> setState { copy(isHelpVisible = true) }
            WelcomeIntent.DismissHelp -> setState { copy(isHelpVisible = false) }
        }
    }
}
