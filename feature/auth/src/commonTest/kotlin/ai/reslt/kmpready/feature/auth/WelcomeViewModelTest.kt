package ai.reslt.kmpready.feature.auth

import ai.reslt.kmpready.core.domain.model.SessionState
import ai.reslt.kmpready.core.domain.usecase.ContinueAsGuestUseCase
import ai.reslt.kmpready.feature.auth.welcome.WelcomeEffect
import ai.reslt.kmpready.feature.auth.welcome.WelcomeIntent
import ai.reslt.kmpready.feature.auth.welcome.WelcomeViewModel
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeViewModelTest {

    private val auth = FakeAuthRepository()
    private lateinit var viewModel: WelcomeViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = WelcomeViewModel(ContinueAsGuestUseCase(auth))
    }

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun signIn_andRegister_emitNavigationEffects() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(WelcomeIntent.SignIn)
            assertEquals(WelcomeEffect.NavigateToSignIn, awaitItem())
            viewModel.onIntent(WelcomeIntent.Register)
            assertEquals(WelcomeEffect.NavigateToRegister, awaitItem())
        }
    }

    @Test
    fun close_entersGuestSession() {
        viewModel.onIntent(WelcomeIntent.Close)
        assertEquals(SessionState.Guest, auth.session.value)
    }

    @Test
    fun help_togglesDialog() {
        viewModel.onIntent(WelcomeIntent.ShowHelp)
        assertTrue(viewModel.state.value.isHelpVisible)
        viewModel.onIntent(WelcomeIntent.DismissHelp)
        assertFalse(viewModel.state.value.isHelpVisible)
    }
}
