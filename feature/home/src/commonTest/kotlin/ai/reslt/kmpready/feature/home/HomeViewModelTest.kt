package ai.reslt.kmpready.feature.home

import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.domain.model.Credentials
import ai.reslt.kmpready.core.domain.model.ExternalSignInMethod
import ai.reslt.kmpready.core.domain.model.LoginChallenge
import ai.reslt.kmpready.core.domain.model.VerificationFactor
import ai.reslt.kmpready.core.domain.model.Money
import ai.reslt.kmpready.core.domain.model.Page
import ai.reslt.kmpready.core.domain.model.PageRequest
import ai.reslt.kmpready.core.domain.model.Product
import ai.reslt.kmpready.core.domain.model.SessionState
import ai.reslt.kmpready.core.domain.model.User
import ai.reslt.kmpready.core.domain.repository.AuthRepository
import ai.reslt.kmpready.core.domain.repository.ProductRepository
import ai.reslt.kmpready.core.domain.usecase.GetCurrentUserUseCase
import ai.reslt.kmpready.core.domain.usecase.GetProductsUseCase
import ai.reslt.kmpready.core.domain.usecase.LogoutUseCase
import ai.reslt.kmpready.core.domain.usecase.ObserveSessionUseCase
import ai.reslt.kmpready.core.ui.toUiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private class FakeProducts(private val total: Int) : ProductRepository {
        var failNext: AppError? = null
        val requests = mutableListOf<PageRequest>()
        override suspend fun getProducts(request: PageRequest): AppResult<Page<Product>> {
            requests += request
            failNext?.let { failNext = null; return AppResult.Failure(it) }
            val end = minOf(request.offset + request.limit, total)
            val items = (request.offset until end).map { product(it.toLong()) }
            return AppResult.Success(Page(items, request.offset, total))
        }
    }

    private class FakeAuth(initial: SessionState = SessionState.LoggedIn) : AuthRepository {
        var loggedOut = false
        var userRequests = 0
        override val session: StateFlow<SessionState> = MutableStateFlow(initial)
        override val pendingChallenge: StateFlow<LoginChallenge?> = MutableStateFlow(null)
        override suspend fun restoreSession() = Unit
        override suspend fun login(credentials: Credentials): AppResult<LoginChallenge> = error("unused")
        override suspend fun verify(factor: VerificationFactor): AppResult<User> = error("unused")
        override suspend fun resendCode(): AppResult<LoginChallenge> = error("unused")
        override fun cancelChallenge() = Unit
        override suspend fun signInWith(method: ExternalSignInMethod): AppResult<User> = error("unused")
        override suspend fun currentUser(): AppResult<User> {
            userRequests++
            return AppResult.Success(User(1, "emilys", "", "Emily", "", null))
        }
        override fun continueAsGuest() = Unit
        override suspend fun logout() { loggedOut = true }
    }

    private lateinit var products: FakeProducts
    private var auth = FakeAuth()

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(total: Int = 45, failFirst: AppError? = null): HomeViewModel {
        products = FakeProducts(total).apply { failNext = failFirst }
        return HomeViewModel(
            GetProductsUseCase(products),
            GetCurrentUserUseCase(auth),
            LogoutUseCase(auth),
            ObserveSessionUseCase(auth),
        )
    }

    @Test
    fun init_loadsUserAndFirstPage() {
        val vm = viewModel()
        val state = vm.state.value
        assertEquals("Emily", state.userName)
        assertEquals(20, state.products.size)
        assertFalse(state.isInitialLoading)
        assertFalse(state.endReached)
    }

    @Test
    fun initialFailure_showsFullScreenError_thenRetryRecovers() {
        val vm = viewModel(failFirst = AppError.Network)
        assertEquals(AppError.Network.toUiText(), vm.state.value.fullScreenError)

        vm.onIntent(HomeIntent.Retry)
        assertEquals(null, vm.state.value.fullScreenError)
        assertEquals(20, vm.state.value.products.size)
    }

    @Test
    fun loadMore_paginatesUntilEnd() {
        val vm = viewModel(total = 45)
        vm.onIntent(HomeIntent.LoadMore)
        vm.onIntent(HomeIntent.LoadMore)
        assertEquals(45, vm.state.value.products.size)
        assertTrue(vm.state.value.endReached)

        vm.onIntent(HomeIntent.LoadMore) // no-op at the end
        assertEquals(3, products.requests.size)
    }

    @Test
    fun guest_skipsProfile_andSignInEndsGuestSession() {
        auth = FakeAuth(SessionState.Guest)
        val vm = viewModel()
        assertTrue(vm.state.value.isGuest)
        assertEquals(null, vm.state.value.userName)
        assertEquals(0, auth.userRequests)
        assertEquals(20, vm.state.value.products.size) // catalogue still public

        vm.onIntent(HomeIntent.SignIn)
        assertTrue(auth.loggedOut)
    }

    @Test
    fun logout_delegatesToRepository() {
        val vm = viewModel()
        vm.onIntent(HomeIntent.Logout)
        assertTrue(auth.loggedOut)
    }

    @Test
    fun discountedProduct_formatsBothPrices() {
        val item = product(1).copy(discountPercent = 10.0).toItem()
        assertEquals("$9.00", item.price)
        assertEquals("$10.00", item.originalPrice)
        assertEquals(10, item.discountPercent)
    }

    private companion object {
        fun product(id: Long) = Product(
            id = id, title = "P$id", description = "", brand = null, category = "c",
            price = Money(1000, "USD"), discountPercent = 0.0, rating = 4.56, stock = 3,
            thumbnailUrl = "", imageUrls = emptyList(),
        )
    }
}
