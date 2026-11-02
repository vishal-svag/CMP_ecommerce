package ai.reslt.kmpready.feature.home

import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.common.result.onSuccess
import ai.reslt.kmpready.core.domain.model.Page
import ai.reslt.kmpready.core.domain.model.SessionState
import ai.reslt.kmpready.core.domain.model.Product
import ai.reslt.kmpready.core.domain.usecase.GetCurrentUserUseCase
import ai.reslt.kmpready.core.domain.usecase.GetProductsUseCase
import ai.reslt.kmpready.core.domain.usecase.LogoutUseCase
import ai.reslt.kmpready.core.domain.usecase.ObserveSessionUseCase
import ai.reslt.kmpready.core.ui.MoneyFormatter
import ai.reslt.kmpready.core.ui.MviViewModel
import ai.reslt.kmpready.core.ui.UiText
import ai.reslt.kmpready.core.ui.toUiText
import ai.reslt.kmpready.feature.home.resources.Res
import ai.reslt.kmpready.feature.home.resources.home_product_coming_soon
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class HomeViewModel(
    private val getProducts: GetProductsUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val logout: LogoutUseCase,
    observeSession: ObserveSessionUseCase,
) : MviViewModel<HomeState, HomeIntent, HomeEffect>(
    HomeState(isGuest = observeSession().value is SessionState.Guest),
) {

    private var pageJob: Job? = null
    private var nextOffset = 0

    init {
        if (!currentState.isGuest) loadUser()
        loadFirstPage(refreshing = false)
    }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Retry -> loadFirstPage(refreshing = false)
            HomeIntent.Refresh -> loadFirstPage(refreshing = true)
            HomeIntent.LoadMore -> loadNextPage()
            is HomeIntent.ProductClicked ->
                sendEffect(HomeEffect.ShowMessage(UiText.Resource(Res.string.home_product_coming_soon)))
            HomeIntent.Logout, HomeIntent.SignIn -> viewModelScope.launch { logout() }
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            getCurrentUser().onSuccess { user -> setState { copy(userName = user.displayName) } }
            // Failure is non-fatal for this screen; a 401 is handled globally by the session.
        }
    }

    private fun loadFirstPage(refreshing: Boolean) {
        pageJob?.cancel()
        setState {
            copy(
                isInitialLoading = !refreshing && products.isEmpty(),
                isRefreshing = refreshing,
                fullScreenError = null,
            )
        }
        pageJob = viewModelScope.launch {
            when (val result = getProducts(offset = 0)) {
                is AppResult.Success -> {
                    nextOffset = result.data.nextOffset
                    setState {
                        copy(
                            products = result.data.items.map { it.toItem() },
                            endReached = !result.data.hasMore,
                            isInitialLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                        )
                    }
                }
                is AppResult.Failure -> {
                    val message = result.error.toUiText()
                    if (currentState.products.isEmpty()) {
                        setState { copy(isInitialLoading = false, isRefreshing = false, fullScreenError = message) }
                    } else {
                        // Keep showing stale content; surface the error transiently.
                        setState { copy(isRefreshing = false) }
                        sendEffect(HomeEffect.ShowMessage(message))
                    }
                }
            }
        }
    }

    private fun loadNextPage() {
        val state = currentState
        if (pageJob?.isActive == true || state.endReached || state.products.isEmpty()) return

        setState { copy(isLoadingMore = true) }
        pageJob = viewModelScope.launch {
            when (val result = getProducts(offset = nextOffset)) {
                is AppResult.Success -> appendPage(result.data)
                is AppResult.Failure -> {
                    setState { copy(isLoadingMore = false) }
                    sendEffect(HomeEffect.ShowMessage(result.error.toUiText()))
                }
            }
        }
    }

    private fun appendPage(page: Page<Product>) {
        nextOffset = page.nextOffset
        setState {
            val known = products.mapTo(HashSet()) { it.id }
            copy(
                products = products + page.items.filterNot { it.id in known }.map { it.toItem() },
                endReached = !page.hasMore,
                isLoadingMore = false,
            )
        }
    }
}

internal fun Product.toItem(): ProductItem = ProductItem(
    id = id,
    title = title,
    imageUrl = thumbnailUrl,
    price = MoneyFormatter.format(finalPrice),
    originalPrice = if (hasDiscount) MoneyFormatter.format(price) else null,
    discountPercent = discountPercent.roundToInt().takeIf { hasDiscount && it > 0 },
    rating = ((rating * 10).roundToInt() / 10.0).toString(),
    inStock = inStock,
)
