package ai.reslt.kmpready.app

import ai.reslt.kmpready.core.designsystem.component.FullScreenLoading
import ai.reslt.kmpready.core.designsystem.component.InstallImageLoader
import ai.reslt.kmpready.core.designsystem.theme.AppTheme
import ai.reslt.kmpready.core.domain.model.SessionState
import ai.reslt.kmpready.core.ui.CommonText
import ai.reslt.kmpready.feature.auth.navigation.LoginRoute
import ai.reslt.kmpready.feature.auth.navigation.WelcomeRoute
import ai.reslt.kmpready.feature.auth.navigation.authGraph
import ai.reslt.kmpready.feature.home.navigation.HomeRoute
import ai.reslt.kmpready.feature.home.navigation.homeGraph
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root composable shared by Android and iOS.
 *
 * Auth gate: the session (not individual screens) decides which graph is shown. Logging in,
 * logging out, or a refresh-token rejection anywhere in the app swaps the graph and throws
 * away the previous back stack, so no authenticated screen can linger after logout.
 */
@Composable
fun App() {
    InstallImageLoader()
    AppTheme {
        val viewModel: AppViewModel = koinViewModel()
        val session by viewModel.session.collectAsStateWithLifecycle()

        Crossfade(targetState = session.toGate(), label = "auth-gate") { gate ->
            when (gate) {
                Gate.Splash -> FullScreenLoading()
                Gate.Main -> MainNavHost()
                is Gate.Unauthenticated -> UnauthenticatedNavHost(expired = gate.expired)
            }
        }
    }
}

private sealed interface Gate {
    data object Splash : Gate
    /** Member or guest — both browse the catalogue; screens adapt via the session. */
    data object Main : Gate
    data class Unauthenticated(val expired: Boolean) : Gate
}

private fun SessionState.toGate(): Gate = when (this) {
    SessionState.Unknown -> Gate.Splash
    SessionState.LoggedIn, SessionState.Guest -> Gate.Main
    is SessionState.LoggedOut -> Gate.Unauthenticated(expired = reason == SessionState.LoggedOut.Reason.Expired)
}

@Composable
private fun UnauthenticatedNavHost(expired: Boolean) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = WelcomeRoute) {
        authGraph(navController, sessionMessage = if (expired) CommonText.sessionExpired else null)
    }
    // Expired session: go straight to sign-in (Welcome stays underneath for back).
    if (expired) {
        LaunchedEffect(Unit) { navController.navigate(LoginRoute) { launchSingleTop = true } }
    }
}

@Composable
private fun MainNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = HomeRoute) {
        homeGraph()
        // Future: productDetailGraph(navController), cartGraph(...), checkoutGraph(...)
    }
}
