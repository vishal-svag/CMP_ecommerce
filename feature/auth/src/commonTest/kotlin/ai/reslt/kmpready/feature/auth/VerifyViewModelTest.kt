package ai.reslt.kmpready.feature.auth

import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.common.time.AppClock
import ai.reslt.kmpready.core.domain.model.SessionState
import ai.reslt.kmpready.core.domain.model.VerificationFactor
import ai.reslt.kmpready.core.domain.usecase.CancelLoginChallengeUseCase
import ai.reslt.kmpready.core.domain.usecase.ObservePendingChallengeUseCase
import ai.reslt.kmpready.core.domain.usecase.ResendCodeUseCase
import ai.reslt.kmpready.core.domain.usecase.VerifyLoginUseCase
import ai.reslt.kmpready.core.ui.toUiText
import ai.reslt.kmpready.feature.auth.verify.VerifyEffect
import ai.reslt.kmpready.feature.auth.verify.VerifyIntent
import ai.reslt.kmpready.feature.auth.verify.VerifyViewModel
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
class VerifyViewModelTest {

    private val repository = FakeAuthRepository()
    private val clock = AppClock { 0L }

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = VerifyViewModel(
        ObservePendingChallengeUseCase(repository),
        VerifyLoginUseCase(repository),
        ResendCodeUseCase(repository),
        CancelLoginChallengeUseCase(repository),
        FakeBiometrics(),
        clock,
        TEST_CONFIG,
    )

    @Test
    fun noPendingChallenge_navigatesBack() = runTest {
        val vm = viewModel()
        vm.effects.test { assertEquals(VerifyEffect.NavigateBack, awaitItem()) }
    }

    @Test
    fun showsIdentity_andResendCountdown() {
        repository.pendingChallenge.value = TEST_CHALLENGE
        val state = viewModel().state.value
        assertEquals("Emily Johnson", state.displayName)
        assertEquals("ATL-00001", state.memberReference)
        assertEquals(45, state.resendSecondsRemaining)
        assertFalse(state.canResend)
    }

    @Test
    fun sixthDigit_autoSubmits_andLogsIn() {
        repository.pendingChallenge.value = TEST_CHALLENGE
        val vm = viewModel()
        vm.onIntent(VerifyIntent.CodeChanged("48"))
        vm.onIntent(VerifyIntent.CodeChanged("482a91x7"))
        assertEquals(listOf<VerificationFactor>(VerificationFactor.Passcode("482917")), repository.verifyCalls)
        assertEquals(SessionState.LoggedIn, repository.session.value)
    }

    @Test
    fun wrongCode_keepsDigits_showsAttempts_andNextDigitStartsOver() {
        repository.pendingChallenge.value = TEST_CHALLENGE
        repository.verifyResults.add(AppResult.Failure(AppError.InvalidCode(3)))
        val vm = viewModel()
        vm.onIntent(VerifyIntent.CodeChanged("000000"))
        assertEquals("000000", vm.state.value.code) // not wiped
        assertEquals(AppError.InvalidCode(3).toUiText(), vm.state.value.codeError)

        vm.onIntent(VerifyIntent.CodeChanged("7")) // what OtpCodeInput emits when typing over a full code
        assertEquals("7", vm.state.value.code)
        assertEquals(null, vm.state.value.codeError)
    }

    @Test
    fun anyFailure_isShownAtTheInput_notOnlyAtBottom() {
        repository.pendingChallenge.value = TEST_CHALLENGE
        repository.verifyResults.add(AppResult.Failure(AppError.Storage))
        val vm = viewModel()
        vm.onIntent(VerifyIntent.CodeChanged("123456"))
        assertEquals("123456", vm.state.value.code)
        assertEquals(AppError.Storage.toUiText(), vm.state.value.codeError)
    }

    @Test
    fun locked_turnsPrimaryIntoBackToSignIn() = runTest {
        repository.pendingChallenge.value = TEST_CHALLENGE
        repository.verifyResults.add(AppResult.Failure(AppError.VerificationLocked))
        val vm = viewModel()
        vm.onIntent(VerifyIntent.CodeChanged("000000"))
        assertTrue(vm.state.value.isLocked)

        vm.effects.test {
            vm.onIntent(VerifyIntent.Submit)
            assertEquals(VerifyEffect.NavigateBack, awaitItem())
        }
    }

    @Test
    fun exit_cancelsChallenge() = runTest {
        repository.pendingChallenge.value = TEST_CHALLENGE
        val vm = viewModel()
        vm.effects.test {
            vm.onIntent(VerifyIntent.Exit)
            assertEquals(VerifyEffect.NavigateBack, awaitItem())
        }
        assertEquals(1, repository.cancelCount)
    }
}
