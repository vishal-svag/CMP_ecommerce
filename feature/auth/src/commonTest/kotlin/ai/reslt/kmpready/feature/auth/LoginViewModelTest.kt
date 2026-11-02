package ai.reslt.kmpready.feature.auth

import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.common.biometric.BiometricAvailability
import ai.reslt.kmpready.core.common.biometric.BiometricKind
import ai.reslt.kmpready.core.domain.model.IdentifierType
import ai.reslt.kmpready.core.domain.usecase.ExternalSignInUseCase
import ai.reslt.kmpready.core.domain.usecase.LoginUseCase
import ai.reslt.kmpready.core.ui.toUiText
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val repository = FakeAuthRepository()
    private lateinit var viewModel: LoginViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = viewModel()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(biometrics: FakeBiometrics = FakeBiometrics()) =
        LoginViewModel(LoginUseCase(repository), ExternalSignInUseCase(repository), biometrics, TEST_CONFIG)

    private fun fillForm(id: String = "emilys", pass: String = "emilyspass") {
        viewModel.onIntent(LoginIntent.IdentifierChanged(id))
        viewModel.onIntent(LoginIntent.PasswordChanged(pass))
    }

    @Test
    fun biometricCaption_followsDeviceCapability() {
        assertEquals(BiometricKind.Face, viewModel.state.value.biometricKind)
        assertNull(viewModel(FakeBiometrics(BiometricAvailability.NotEnrolled)).state.value.biometricKind)
    }

    @Test
    fun blankIdentifier_showsFieldError_andNoNetworkCall() {
        fillForm(id = "  ")
        viewModel.onIntent(LoginIntent.Submit)
        assertNotNull(viewModel.state.value.identifierError)
        assertTrue(repository.loginCalls.isEmpty())
    }

    @Test
    fun switchingTab_clearsIdentifier_andValidatesMemberNumber() {
        fillForm(id = "emilys")
        viewModel.onIntent(LoginIntent.IdentifierTypeChanged(IdentifierType.MemberNumber))
        assertEquals("", viewModel.state.value.identifier)

        viewModel.onIntent(LoginIntent.IdentifierChanged("AB12"))
        viewModel.onIntent(LoginIntent.Submit)
        assertNotNull(viewModel.state.value.identifierError)
    }

    @Test
    fun success_navigatesToVerification_passesRememberMe_andWipesPassword() = runTest {
        fillForm()
        viewModel.onIntent(LoginIntent.RememberMeChanged(true))
        viewModel.effects.test {
            viewModel.onIntent(LoginIntent.Submit)
            assertEquals(LoginEffect.NavigateToVerification, awaitItem())
        }
        assertTrue(repository.loginCalls.single().rememberMe)
        assertEquals("", viewModel.state.value.password)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun invalidCredentials_showsFormError() {
        repository.loginResult = AppResult.Failure(AppError.InvalidCredentials)
        fillForm()
        viewModel.onIntent(LoginIntent.Submit)
        assertEquals(AppError.InvalidCredentials.toUiText(), viewModel.state.value.formError)
    }

    @Test
    fun doubleSubmit_whileLoading_sendsOneRequest() {
        repository.gate = CompletableDeferred()
        fillForm()
        viewModel.onIntent(LoginIntent.Submit)
        assertTrue(viewModel.state.value.isLoading)
        viewModel.onIntent(LoginIntent.Submit)
        repository.gate!!.complete(Unit)
        assertEquals(1, repository.loginCalls.size)
    }

    @Test
    fun apple_notYetAvailable_showsMessage() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(LoginIntent.SignInWithApple)
            assertIs<LoginEffect.ShowMessage>(awaitItem())
        }
    }

    @Test
    fun conciergeDesk_opensConfiguredUri() = runTest {
        viewModel.effects.test {
            viewModel.onIntent(LoginIntent.ContactConcierge)
            assertEquals(LoginEffect.OpenUri(TEST_CONFIG.conciergeEmailUri!!), awaitItem())
        }
    }
}
