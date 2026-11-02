package ai.reslt.kmpready.core.domain

import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.error.AppError.Validation.Field
import ai.reslt.kmpready.core.common.error.AppError.Validation.Reason
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.domain.model.Credentials
import ai.reslt.kmpready.core.domain.model.IdentifierType
import ai.reslt.kmpready.core.domain.model.VerificationFactor
import ai.reslt.kmpready.core.domain.usecase.LoginUseCase
import ai.reslt.kmpready.core.domain.usecase.VerifyLoginUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LoginUseCaseTest {
    private val repository = FakeAuthRepository()
    private val login = LoginUseCase(repository)
    private val verify = VerifyLoginUseCase(repository)

    @Test
    fun blankIdentifier_failsWithoutCallingRepository() = runTest {
        val result = login(Credentials("   ", "password123"))
        assertEquals(AppResult.Failure(AppError.Validation(Field.Identifier, Reason.Blank)), result)
        assertTrue(repository.loginCalls.isEmpty())
    }

    @Test
    fun memberNumber_mustBeNumeric() = runTest {
        val result = login(Credentials("ATL-12", "password123", IdentifierType.MemberNumber))
        assertEquals(AppResult.Failure(AppError.Validation(Field.Identifier, Reason.Invalid)), result)
    }

    @Test
    fun shortPassword_fails() = runTest {
        val result = login(Credentials("emily", "123"))
        assertEquals(AppResult.Failure(AppError.Validation(Field.Password, Reason.TooShort)), result)
    }

    @Test
    fun validInput_trimsIdentifierAndDelegates() = runTest {
        val result = login(Credentials("  emilys ", "emilyspass"))
        assertIs<AppResult.Success<*>>(result)
        assertEquals("emilys", repository.loginCalls.single().identifier)
    }

    @Test
    fun passcode_mustBeExactDigits() = runTest {
        assertEquals(AppResult.Failure(AppError.Validation(Field.Code, Reason.Invalid)), verify(VerificationFactor.Passcode("12a456")))
        assertEquals(AppResult.Failure(AppError.Validation(Field.Code, Reason.Invalid)), verify(VerificationFactor.Passcode("12345")))
        assertTrue(repository.verifyCalls.isEmpty())
        assertIs<AppResult.Success<*>>(verify(VerificationFactor.Passcode("123456")))
    }

    @Test
    fun secrets_neverAppearInToString() {
        assertTrue("secret-pass" !in Credentials("u", "secret-pass").toString())
        assertTrue("987654" !in VerificationFactor.Passcode("987654").toString())
    }
}
