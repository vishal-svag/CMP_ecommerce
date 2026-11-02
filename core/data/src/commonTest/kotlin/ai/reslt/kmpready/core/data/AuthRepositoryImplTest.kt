package ai.reslt.kmpready.core.data

import ai.reslt.kmpready.core.common.config.AppConfig
import ai.reslt.kmpready.core.common.coroutines.DispatcherProvider
import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.data.remote.api.AuthApi
import ai.reslt.kmpready.core.data.repository.AuthRepositoryImpl
import ai.reslt.kmpready.core.data.verification.DevSecondFactorService
import ai.reslt.kmpready.core.domain.model.Credentials
import ai.reslt.kmpready.core.domain.model.SessionState
import ai.reslt.kmpready.core.domain.model.VerificationFactor
import ai.reslt.kmpready.core.network.HttpClientFactory
import ai.reslt.kmpready.core.network.auth.SessionEvents
import ai.reslt.kmpready.core.security.AuthTokens
import ai.reslt.kmpready.core.security.SecureStorage
import ai.reslt.kmpready.core.security.TokenStore
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {

    private class MemoryStorage : SecureStorage {
        val map = mutableMapOf<String, String>()
        override suspend fun get(key: String) = map[key]
        override suspend fun put(key: String, value: String) { map[key] = value }
        override suspend fun remove(key: String) { map.remove(key) }
        override suspend fun clear() = map.clear()
    }

    private class TestDispatchers(d: CoroutineDispatcher) : DispatcherProvider {
        override val main = d
        override val io = d
        override val default = d
    }

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private val storage = MemoryStorage()
    private var now = 0L
    private val tokenStore = TokenStore(storage) { now }

    /** Same seed => the test can derive the code the service will generate. */
    private fun expectedCode(seed: Int): String {
        val r = Random(seed)
        r.nextLong(Long.MAX_VALUE) // challenge id
        return (0 until 6).joinToString("") { r.nextInt(10).toString() }
    }

    private fun TestScope.repository(engine: MockEngine, seed: Int = 42): AuthRepositoryImpl {
        val events = SessionEvents()
        val client = HttpClientFactory(AppConfig("https://api.test/", isDebug = false), tokenStore, events).create(engine)
        return AuthRepositoryImpl(
            api = AuthApi(client),
            tokenStore = tokenStore,
            secondFactor = DevSecondFactorService(clock = { now }, random = Random(seed)),
            onTokensChanged = {},
            sessionEvents = events,
            appScope = backgroundScope,
            dispatchers = TestDispatchers(StandardTestDispatcher(testScheduler)),
        )
    }

    private val okLogin = MockEngine {
        respond(
            """{"id":1,"username":"emilys","firstName":"Emily","lastName":"Johnson","accessToken":"a","refreshToken":"r"}""",
            HttpStatusCode.OK,
            jsonHeaders,
        )
    }

    @Test
    fun login_issuesChallenge_withoutStartingSession() = runTest {
        val repo = repository(okLogin)

        val result = repo.login(Credentials("emilys", "emilyspass"))

        val challenge = assertIs<AppResult.Success<*>>(result).data
        assertNotNull(challenge)
        assertEquals("Emily Johnson", repo.pendingChallenge.value?.displayName)
        assertEquals("ATL-00001", repo.pendingChallenge.value?.memberReference)
        assertIs<SessionState.Unknown>(repo.session.value)
        assertNull(tokenStore.read()) // nothing usable before step 2
    }

    @Test
    fun verify_correctCode_rememberMe_persistsAndLogsIn() = runTest {
        val repo = repository(okLogin, seed = 7)
        repo.login(Credentials("emilys", "emilyspass", rememberMe = true))

        val result = repo.verify(VerificationFactor.Passcode(expectedCode(7)))

        assertIs<AppResult.Success<*>>(result)
        assertEquals(SessionState.LoggedIn, repo.session.value)
        assertNull(repo.pendingChallenge.value)
        assertEquals("a", storage.map["auth.access_token"])
    }

    @Test
    fun verify_withoutRememberMe_keepsTokensOffDisk() = runTest {
        val repo = repository(okLogin, seed = 7)
        repo.login(Credentials("emilys", "emilyspass", rememberMe = false))
        repo.verify(VerificationFactor.Passcode(expectedCode(7)))

        assertEquals(AuthTokens("a", "r"), tokenStore.read())
        assertTrue(storage.map.isEmpty())
    }

    @Test
    fun wrongCode_countsDown_thenLocksAndDropsChallenge() = runTest {
        val repo = repository(okLogin, seed = 7)
        repo.login(Credentials("emilys", "emilyspass"))
        val wrong = if (expectedCode(7) == "000000") "111111" else "000000"

        repeat(DevSecondFactorService.MAX_ATTEMPTS - 1) { i ->
            val r = repo.verify(VerificationFactor.Passcode(wrong))
            assertEquals(AppResult.Failure(AppError.InvalidCode(DevSecondFactorService.MAX_ATTEMPTS - 1 - i)), r)
        }
        assertEquals(AppResult.Failure(AppError.VerificationLocked), repo.verify(VerificationFactor.Passcode(wrong)))
        assertNull(repo.pendingChallenge.value)
    }

    @Test
    fun expiredCode_isRejected() = runTest {
        val repo = repository(okLogin, seed = 7)
        repo.login(Credentials("emilys", "emilyspass"))
        now += DevSecondFactorService.TTL_MS

        assertEquals(AppResult.Failure(AppError.CodeExpired), repo.verify(VerificationFactor.Passcode(expectedCode(7))))
    }

    @Test
    fun resend_respectsCooldown() = runTest {
        val repo = repository(okLogin)
        repo.login(Credentials("emilys", "emilyspass"))

        assertEquals(AppResult.Failure(AppError.TooManyRequests), repo.resendCode())
        now += DevSecondFactorService.RESEND_COOLDOWN_MS
        assertIs<AppResult.Success<*>>(repo.resendCode())
    }

    @Test
    fun login_badCredentials_mapsToInvalidCredentials() = runTest {
        val repo = repository(MockEngine { respondError(HttpStatusCode.BadRequest) })
        assertEquals(AppResult.Failure(AppError.InvalidCredentials), repo.login(Credentials("emilys", "wrong-pass")))
        assertNull(repo.pendingChallenge.value)
    }

    @Test
    fun cancelChallenge_clearsPending() = runTest {
        val repo = repository(okLogin)
        repo.login(Credentials("emilys", "emilyspass"))
        repo.cancelChallenge()
        assertNull(repo.pendingChallenge.value)
        assertEquals(AppResult.Failure(AppError.NoPendingVerification), repo.verify(VerificationFactor.Passcode("123456")))
    }

    @Test
    fun logout_clearsTokens() = runTest {
        tokenStore.start(AuthTokens("a", "r"), persistForMillis = 60_000)
        val repo = repository(MockEngine { respondError(HttpStatusCode.InternalServerError) })

        repo.logout()

        assertNull(tokenStore.read())
        assertEquals(SessionState.LoggedOut(SessionState.LoggedOut.Reason.UserInitiated), repo.session.value)
    }

    @Test
    fun continueAsGuest_onlyFromLoggedOut() = runTest {
        val repo = repository(MockEngine { respondError(HttpStatusCode.InternalServerError) })

        repo.continueAsGuest()
        assertIs<SessionState.Unknown>(repo.session.value)

        repo.restoreSession()
        repo.continueAsGuest()
        assertEquals(SessionState.Guest, repo.session.value)
    }
}
