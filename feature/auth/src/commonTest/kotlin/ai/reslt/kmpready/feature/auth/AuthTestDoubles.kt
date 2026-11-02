package ai.reslt.kmpready.feature.auth

import ai.reslt.kmpready.core.common.config.AppConfig
import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.common.biometric.BiometricAuthenticator
import ai.reslt.kmpready.core.common.biometric.BiometricAvailability
import ai.reslt.kmpready.core.common.biometric.BiometricKind
import ai.reslt.kmpready.core.common.biometric.BiometricRequest
import ai.reslt.kmpready.core.common.biometric.BiometricResult
import ai.reslt.kmpready.core.domain.model.Credentials
import ai.reslt.kmpready.core.domain.model.ExternalSignInMethod
import ai.reslt.kmpready.core.domain.model.LoginChallenge
import ai.reslt.kmpready.core.domain.model.SessionState
import ai.reslt.kmpready.core.domain.model.User
import ai.reslt.kmpready.core.domain.model.VerificationFactor
import ai.reslt.kmpready.core.domain.repository.AuthRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

internal val TEST_USER = User(1, "emilys", "", "Emily", "Johnson", null)
internal val TEST_CHALLENGE = LoginChallenge(
    id = "c1",
    displayName = "Emily Johnson",
    memberReference = "ATL-00001",
    codeLength = 6,
    expiresAtMillis = 300_000,
    resendAvailableAtMillis = 45_000,
)
internal val TEST_CONFIG = AppConfig(baseUrl = "https://example.com/", isDebug = true)

internal class FakeAuthRepository : AuthRepository {
    override val session = MutableStateFlow<SessionState>(SessionState.LoggedOut())
    override val pendingChallenge = MutableStateFlow<LoginChallenge?>(null)

    var loginResult: AppResult<LoginChallenge> = AppResult.Success(TEST_CHALLENGE)
    var verifyResults = ArrayDeque<AppResult<User>>()
    var gate: CompletableDeferred<Unit>? = null
    val loginCalls = mutableListOf<Credentials>()
    val verifyCalls = mutableListOf<VerificationFactor>()
    var cancelCount = 0

    override suspend fun restoreSession() = Unit
    override suspend fun login(credentials: Credentials): AppResult<LoginChallenge> {
        loginCalls += credentials
        gate?.await()
        if (loginResult is AppResult.Success) pendingChallenge.value = (loginResult as AppResult.Success).data
        return loginResult
    }
    override suspend fun verify(factor: VerificationFactor): AppResult<User> {
        verifyCalls += factor
        val result = verifyResults.removeFirstOrNull() ?: AppResult.Success(TEST_USER)
        if (result is AppResult.Success) {
            pendingChallenge.value = null
            session.value = SessionState.LoggedIn
        }
        return result
    }
    override suspend fun resendCode(): AppResult<LoginChallenge> = AppResult.Success(TEST_CHALLENGE)
    override fun cancelChallenge() {
        cancelCount++
        pendingChallenge.value = null
    }
    override suspend fun signInWith(method: ExternalSignInMethod): AppResult<User> = AppResult.Failure(AppError.Unavailable)
    override suspend fun currentUser(): AppResult<User> = AppResult.Success(TEST_USER)
    override fun continueAsGuest() { session.value = SessionState.Guest }
    override suspend fun logout() = Unit
}

internal class FakeBiometrics(
    private val available: BiometricAvailability = BiometricAvailability.Available(BiometricKind.Face),
    var result: BiometricResult = BiometricResult.Success,
) : BiometricAuthenticator {
    override fun availability() = available
    override suspend fun authenticate(request: BiometricRequest) = result
}
