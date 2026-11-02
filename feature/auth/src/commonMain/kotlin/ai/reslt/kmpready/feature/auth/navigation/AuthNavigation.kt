package ai.reslt.kmpready.feature.auth.navigation

import ai.reslt.kmpready.core.ui.UiText
import ai.reslt.kmpready.feature.auth.login.LoginScreen
import ai.reslt.kmpready.feature.auth.register.RegisterScreen
import ai.reslt.kmpready.feature.auth.verify.VerifyScreen
import ai.reslt.kmpready.feature.auth.welcome.WelcomeScreen
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object WelcomeRoute

@Serializable
data object LoginRoute

/** Step 2. No arguments on purpose — the challenge lives in the repository, never in the back stack. */
@Serializable
data object VerifyRoute

@Serializable
data object RegisterRoute

/**
 * Public navigation API of the auth feature: Welcome → Sign in → VIP Security Check.
 * Successful verification / guest entry are session changes handled by the app's auth gate.
 */
fun NavGraphBuilder.authGraph(navController: NavController, sessionMessage: UiText?) {
    composable<WelcomeRoute> {
        WelcomeScreen(
            onNavigateToSignIn = { navController.navigate(LoginRoute) { launchSingleTop = true } },
            onNavigateToRegister = { navController.navigate(RegisterRoute) { launchSingleTop = true } },
        )
    }
    composable<LoginRoute> {
        LoginScreen(
            onBack = { navController.popBackStack() },
            onNavigateToVerification = { navController.navigate(VerifyRoute) { launchSingleTop = true } },
            sessionMessage = sessionMessage,
        )
    }
    composable<VerifyRoute> {
        VerifyScreen(
            onNavigateBack = {
                if (!navController.popBackStack(LoginRoute, inclusive = false)) {
                    navController.navigate(LoginRoute) { popUpTo(WelcomeRoute) }
                }
            },
        )
    }
    composable<RegisterRoute> {
        RegisterScreen(
            onBack = { navController.popBackStack() },
            onSignIn = {
                navController.navigate(LoginRoute) {
                    popUpTo(WelcomeRoute)
                    launchSingleTop = true
                }
            },
        )
    }
}
