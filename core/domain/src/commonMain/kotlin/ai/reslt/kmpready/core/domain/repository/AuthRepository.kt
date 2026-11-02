package ai.reslt.kmpready.core.domain.repository

import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.domain.model.Credentials
import ai.reslt.kmpready.core.domain.model.ExternalSignInMethod
import ai.reslt.kmpready.core.domain.model.LoginChallenge
import ai.reslt.kmpready.core.domain.model.SessionState
import ai.reslt.kmpready.core.domain.model.User
import ai.reslt.kmpready.core.domain.model.VerificationFactor
import kotlinx.coroutines.flow.StateFlow

/**
 * Two-step sign-in:
 *   login(credentials) -> LoginChallenge   (session still LoggedOut)
 *   verify(factor)     -> User             (session becomes LoggedIn)
 */
interface AuthRepository {
    val session: StateFlow<SessionState>

    /** Challenge awaiting a second factor, or null. In-memory only. */
    val pendingChallenge: StateFlow<LoginChallenge?>

    /** Reads persisted tokens and moves [session] out of [SessionState.Unknown]. */
    suspend fun restoreSession()

    suspend fun login(credentials: Credentials): AppResult<LoginChallenge>

    suspend fun verify(factor: VerificationFactor): AppResult<User>

    suspend fun resendCode(): AppResult<LoginChallenge>

    /** Abandons step 2 and wipes any credentials held for it. */
    fun cancelChallenge()

    suspend fun signInWith(method: ExternalSignInMethod): AppResult<User>

    suspend fun currentUser(): AppResult<User>

    /** LoggedOut -> Guest. No-op from any other state. */
    fun continueAsGuest()

    /** Ends a member session (wipes tokens) or a guest session. */
    suspend fun logout()
}
