package ai.reslt.kmpready.core.domain

import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.domain.model.Credentials
import ai.reslt.kmpready.core.domain.model.ExternalSignInMethod
import ai.reslt.kmpready.core.domain.model.LoginChallenge
import ai.reslt.kmpready.core.domain.model.SessionState
import ai.reslt.kmpready.core.domain.model.User
import ai.reslt.kmpready.core.domain.model.VerificationFactor
import ai.reslt.kmpready.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthRepository : AuthRepository {
    val loginCalls = mutableListOf<Credentials>()
    val verifyCalls = mutableListOf<VerificationFactor>()
    private val _session = MutableStateFlow<SessionState>(SessionState.Unknown)
    override val session: StateFlow<SessionState> = _session
    override val pendingChallenge = MutableStateFlow<LoginChallenge?>(null)

    override suspend fun restoreSession() { _session.value = SessionState.LoggedOut() }
    override suspend fun login(credentials: Credentials): AppResult<LoginChallenge> {
        loginCalls += credentials
        pendingChallenge.value = TEST_CHALLENGE
        return AppResult.Success(TEST_CHALLENGE)
    }
    override suspend fun verify(factor: VerificationFactor): AppResult<User> {
        verifyCalls += factor
        return AppResult.Success(TEST_USER)
    }
    override suspend fun resendCode(): AppResult<LoginChallenge> = AppResult.Success(TEST_CHALLENGE)
    override fun cancelChallenge() { pendingChallenge.value = null }
    override suspend fun signInWith(method: ExternalSignInMethod): AppResult<User> = AppResult.Failure(AppError.Unavailable)
    override suspend fun currentUser(): AppResult<User> = AppResult.Success(TEST_USER)
    override fun continueAsGuest() { _session.value = SessionState.Guest }
    override suspend fun logout() { _session.value = SessionState.LoggedOut(SessionState.LoggedOut.Reason.UserInitiated) }

    companion object {
        val TEST_USER = User(1, "emilys", "emily@example.com", "Emily", "Johnson", null)
        val TEST_CHALLENGE = LoginChallenge("c1", "Emily Johnson", "ATL-00001", 6, 300_000, 45_000)
    }
}
