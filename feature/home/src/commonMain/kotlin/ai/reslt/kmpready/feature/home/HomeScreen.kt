package ai.reslt.kmpready.feature.home

import ai.reslt.kmpready.core.designsystem.component.EmptyState
import ai.reslt.kmpready.core.designsystem.component.ErrorState
import ai.reslt.kmpready.core.designsystem.component.FullScreenLoading
import ai.reslt.kmpready.core.designsystem.component.ProductCard
import ai.reslt.kmpready.core.designsystem.theme.AppTheme
import ai.reslt.kmpready.core.ui.CommonText
import ai.reslt.kmpready.core.ui.ObserveAsEvents
import ai.reslt.kmpready.core.ui.asString
import ai.reslt.kmpready.core.ui.resolve
import ai.reslt.kmpready.feature.home.resources.Res
import ai.reslt.kmpready.feature.home.resources.home_discount
import ai.reslt.kmpready.feature.home.resources.home_empty
import ai.reslt.kmpready.feature.home.resources.home_greeting
import ai.reslt.kmpready.feature.home.resources.home_greeting_fallback
import ai.reslt.kmpready.feature.home.resources.home_logout
import ai.reslt.kmpready.feature.home.resources.home_out_of_stock
import ai.reslt.kmpready.feature.home.resources.home_rating
import ai.reslt.kmpready.feature.home.resources.home_sign_in
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.effects) { effect ->
        when (effect) {
            is HomeEffect.ShowMessage -> {
                snackbarHostState.showSnackbar(effect.text.resolve())
            }
        }
    }

    HomeContent(state = state, snackbarHostState = snackbarHostState, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.userName?.let { stringResource(Res.string.home_greeting, it) }
                            ?: stringResource(Res.string.home_greeting_fallback),
                    )
                },
                actions = {
                    if (state.isGuest) {
                        TextButton(onClick = { onIntent(HomeIntent.SignIn) }) {
                            Text(stringResource(Res.string.home_sign_in).uppercase(), style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        TextButton(onClick = { onIntent(HomeIntent.Logout) }) {
                            Text(stringResource(Res.string.home_logout).uppercase(), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val contentModifier = Modifier.fillMaxSize().padding(padding)
        when {
            state.isInitialLoading -> FullScreenLoading(contentModifier)
            state.fullScreenError != null -> ErrorState(
                message = state.fullScreenError.asString(),
                retryLabel = CommonText.retry.asString(),
                onRetry = { onIntent(HomeIntent.Retry) },
                modifier = contentModifier,
            )
            else -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(HomeIntent.Refresh) },
                modifier = contentModifier,
            ) {
                if (state.products.isEmpty()) {
                    EmptyState(stringResource(Res.string.home_empty))
                } else {
                    ProductGrid(state = state, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
private fun ProductGrid(state: HomeState, onIntent: (HomeIntent) -> Unit) {
    val gridState = rememberLazyGridState()
    LoadMoreWhenNearEnd(gridState, enabled = !state.endReached && !state.isLoadingMore) {
        onIntent(HomeIntent.LoadMore)
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(AppTheme.spacing.screen),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(state.products, key = { it.id }, contentType = { "product" }) { item ->
            ProductCard(
                title = item.title,
                imageUrl = item.imageUrl,
                price = item.price,
                originalPrice = item.originalPrice,
                discountLabel = item.discountPercent?.let { stringResource(Res.string.home_discount, it) },
                ratingLabel = stringResource(Res.string.home_rating, item.rating),
                outOfStockLabel = if (item.inStock) null else stringResource(Res.string.home_out_of_stock),
                onClick = { onIntent(HomeIntent.ProductClicked(item.id)) },
            )
        }
        if (state.isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }, contentType = "loader") {
                Box(Modifier.fillMaxWidth().padding(AppTheme.spacing.lg), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun LoadMoreWhenNearEnd(state: LazyGridState, enabled: Boolean, threshold: Int = 6, onLoadMore: () -> Unit) {
    LaunchedEffect(state, enabled) {
        if (!enabled) return@LaunchedEffect
        snapshotFlow {
            val info = state.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - threshold
        }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMore() }
    }
}

@Preview
@Composable
private fun HomeContentPreview() {
    AppTheme {
        HomeContent(
            state = HomeState(
                userName = "Emily",
                isInitialLoading = false,
                products = List(4) {
                    ProductItem(it.toLong(), "Essence Mascara Lash Princess", "", "$9.49", "$9.99", 5, "4.9", true)
                },
            ),
            onIntent = {},
        )
    }
}
