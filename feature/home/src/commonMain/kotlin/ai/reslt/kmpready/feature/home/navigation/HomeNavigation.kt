package ai.reslt.kmpready.feature.home.navigation

import ai.reslt.kmpready.feature.home.HomeScreen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

fun NavGraphBuilder.homeGraph() {
    composable<HomeRoute> { HomeScreen() }
}
