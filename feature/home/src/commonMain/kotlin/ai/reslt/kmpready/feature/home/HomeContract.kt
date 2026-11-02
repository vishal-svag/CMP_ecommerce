package ai.reslt.kmpready.feature.home

import ai.reslt.kmpready.core.ui.UiText
import androidx.compose.runtime.Immutable

/** Presentation model: everything pre-formatted so the composable does zero logic. */
@Immutable
data class ProductItem(
    val id: Long,
    val title: String,
    val imageUrl: String,
    val price: String,
    val originalPrice: String?,
    val discountPercent: Int?,
    val rating: String,
    val inStock: Boolean,
)

@Immutable
data class HomeState(
    /** Guest browsing: no profile, account action becomes "Sign in". */
    val isGuest: Boolean = false,
    val userName: String? = null,
    val products: List<ProductItem> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val fullScreenError: UiText? = null,
)

sealed interface HomeIntent {
    data object Retry : HomeIntent
    data object Refresh : HomeIntent
    data object LoadMore : HomeIntent
    data class ProductClicked(val id: Long) : HomeIntent
    data object Logout : HomeIntent
    /** Guest wants to become a member: ends the guest session, auth gate shows Welcome. */
    data object SignIn : HomeIntent
}

sealed interface HomeEffect {
    data class ShowMessage(val text: UiText) : HomeEffect
}
