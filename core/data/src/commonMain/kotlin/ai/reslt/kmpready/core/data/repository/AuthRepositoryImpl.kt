package ai.reslt.kmpready.core.data.repository

import ai.reslt.kmpready.core.common.coroutines.DispatcherProvider
import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.logging.AppLogger
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.common.result.mapError
import ai.reslt.kmpready.core.data.mapper.toDomain
import ai.reslt.kmpready.core.data.mapper.toUser
import ai.reslt.kmpready.core.data.remote.api.AuthApi
import ai.reslt.kmpready.core.data.remote.dto.LoginRequestDto
import ai.reslt.kmpready.core.data.verification.IssuedChallenge
import ai.reslt.kmpready.core.data.verification.SecondFactorService
import ai.reslt.kmpready.core.domain.model.Credentials
import ai.reslt.kmpready.core.domain.model.ExternalSignInMethod
import ai.reslt.kmpready.core.domain.model.LoginChallenge
import ai.reslt.kmpready.core.domain.model.SessionState
import ai.reslt.kmpready.core.domain.model.SessionState.LoggedOut.Reason
import ai.reslt.kmpready.core.domain.model.User
import ai.reslt.kmpready.core.domain.model.VerificationFactor
import ai.reslt.kmpready.core.domain.repository.AuthRepository
import ai.reslt.kmpready.core.network.auth.SessionEvent
import ai.reslt.kmpready.core.network.auth.SessionEvents
import ai.reslt.kmpready.core.network.safeApiCall
import ai.reslt.kmpready.core.security.AuthTokens
import ai.reslt.kmpready.core.security.TokenStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AuthRepositoryImpl(
    private val api: AuthApi,
    private val tokenStore: TokenStore,
    private val secondFactor: SecondFactorService,
    private val onTokensChanged: () -> Unit,
    sessionEvents: SessionEvents,
    appScope: CoroutineScope,
    private val dispatchers: DispatcherProvider,
) : AuthRepository {

    /**
     * Step-1 result held in memory only until step 2 succeeds.
     * NOTE (backend): a production API must NOT issue full session tokens before the second
     * factor — it should return a short-lived MFA token instead. DummyJSON can't, so we hold
     * its tokens here and never expose them to the HTTP client until verification succeeds.
     */
    private class Pending(
        val challenge: LoginChallenge,
        val tokens: AuthTokens,
        val user: User,
        val rememberMe: Boolean,
    )

    private val log = AppLogger.withTag("Auth")
    private val _session = MutableStateFlow<SessionState>(SessionState.Unknown)
    override val session: StateFlow<SessionState> = _session.asStateFlow()

    // Updated synchronously (no stateIn) so the next screen can read the challenge immediately.
    private val pending = MutableStateFlow<Pending?>(null)
    private val _pendingChallenge = MutableStateFlow<LoginChallenge?>(null)
    override val pendingChallenge: StateFlow<LoginChallenge?> = _pendingChallenge.asStateFlow()

    private fun setPending(value: Pending?) {
        pending.value = value
        _pendingChallenge.value = value?.challenge
    }

    init {
        appScope.launch {
            sessionEvents.events.collect { event ->
                when (event) {
                    SessionEvent.Expired -> {
                        onTokensChanged()
                        _session.value = SessionState.LoggedOut(Reason.Expired)
                    }
                }
            }
        }
    }

    override suspend fun restoreSession() {
        val tokens = withContext(dispatchers.io) { runCatchingStorage { tokenStore.read() } }
        _session.value = if (tokens is AppResult.Success && tokens.data != null) {
            SessionState.LoggedIn
        } else {
            SessionState.LoggedOut()
        }
    }

    override suspend fun login(credentials: Credentials): AppResult<LoginChallenge> = withContext(dispatchers.io) {
        cancelChallenge()
        val response = safeApiCall { api.login(LoginRequestDto(credentials.identifier, credentials.password)) }
            .mapError { error ->
                // The API answers 400/401 for wrong credentials; surface a dedicated error.
                when (error) {
                    AppError.Unauthorized, is AppError.Client -> AppError.InvalidCredentials
                    else -> error
                }
            }
        val dto = when (response) {
            is AppResult.Success -> response.data
            is AppResult.Failure -> return@withContext response
        }
        val user = dto.toUser()
        when (val issued = secondFactor.issue(user.id)) {
            is AppResult.Failure -> issued
            is AppResult.Success -> {
                val challenge = issued.data.toChallenge(user)
                setPending(Pending(challenge, AuthTokens(dto.accessToken, dto.refreshToken), user, credentials.rememberMe))
                AppResult.Success(challenge)
            }
        }
    }

    override suspend fun verify(factor: VerificationFactor): AppResult<User> = withContext(dispatchers.io) {
        val current = pending.value ?: return@withContext AppResult.Failure(AppError.NoPendingVerification)
        when (val result = secondFactor.verify(current.challenge.id, factor)) {
            is AppResult.Failure -> {
                if (result.error == AppError.VerificationLocked || result.error == AppError.NoPendingVerification) {
                    setPending(null)
                }
                result
            }
            is AppResult.Success -> {
                val persistFor = if (current.rememberMe) REMEMBER_ME_MILLIS else null
                val started = runCatchingStorage {
                    tokenStore.start(current.tokens, persistForMillis = persistFor)
                    onTokensChanged()
                }
                if (started is AppResult.Failure) return@withContext started
                setPending(null)
                _session.value = SessionState.LoggedIn
                AppResult.Success(current.user)
            }
        }
    }

    override suspend fun resendCode(): AppResult<LoginChallenge> = withContext(dispatchers.io) {
        val current = pending.value ?: return@withContext AppResult.Failure(AppError.NoPendingVerification)
        when (val result = secondFactor.resend(current.challenge.id)) {
            is AppResult.Failure -> result
            is AppResult.Success -> {
                val challenge = result.data.toChallenge(current.user)
                setPending(Pending(challenge, current.tokens, current.user, current.rememberMe))
                AppResult.Success(challenge)
            }
        }
    }

    override fun cancelChallenge() {
        val current = pending.value ?: return
        secondFactor.discard(current.challenge.id)
        setPending(null)
    }

    override suspend fun signInWith(method: ExternalSignInMethod): AppResult<User> {
        // TODO(backend): Apple -> verify identity token server-side; Passkey -> WebAuthn ceremony.
        log.i { "External sign-in requested: $method (not yet available)" }
        return AppResult.Failure(AppError.Unavailable)
    }

    override suspend fun currentUser(): AppResult<User> = withContext(dispatchers.io) {
        safeApiCall { api.me().toDomain() }
    }

    override fun continueAsGuest() {
        _session.update { current -> if (current is SessionState.LoggedOut) SessionState.Guest else current }
    }

    override suspend fun logout() {
        cancelChallenge()
        withContext(dispatchers.io) {
            // TODO(backend): call the token revocation endpoint (best effort) once it exists.
            runCatchingStorage { tokenStore.clear() }
            onTokensChanged()
        }
        _session.value = SessionState.LoggedOut(Reason.UserInitiated)
    }

    private fun IssuedChallenge.toChallenge(user: User) = LoginChallenge(
        id = id,
        displayName = "${user.firstName} ${user.lastName}".trim().ifBlank { user.username },
        memberReference = "ATL-" + user.id.toString().padStart(5, '0'),
        codeLength = codeLength,
        expiresAtMillis = expiresAtMillis,
        resendAvailableAtMillis = resendAvailableAtMillis,
    )

    private suspend fun <T> runCatchingStorage(block: suspend () -> T): AppResult<T> = try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        log.e(e) { "Secure storage failure" }
        AppResult.Failure(AppError.Storage)
    }

    private companion object {
        const val REMEMBER_ME_MILLIS = Credentials.REMEMBER_ME_DAYS * 24L * 60 * 60 * 1000
    }
}
