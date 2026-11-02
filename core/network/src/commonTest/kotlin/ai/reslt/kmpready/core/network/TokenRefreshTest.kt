package ai.reslt.kmpready.core.network

import ai.reslt.kmpready.core.common.config.AppConfig
import ai.reslt.kmpready.core.network.auth.SessionEvent
import ai.reslt.kmpready.core.network.auth.SessionEvents
import ai.reslt.kmpready.core.security.AuthTokens
import ai.reslt.kmpready.core.security.SecureStorage
import ai.reslt.kmpready.core.security.TokenStore
import app.cash.turbine.test
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TokenRefreshTest {

    private class MemoryStorage : SecureStorage {
        val map = mutableMapOf<String, String>()
        override suspend fun get(key: String) = map[key]
        override suspend fun put(key: String, value: String) { map[key] = value }
        override suspend fun remove(key: String) { map.remove(key) }
        override suspend fun clear() = map.clear()
    }

    private val config = AppConfig(baseUrl = "https://api.test/", isDebug = false)
    private val tokenStore = TokenStore(MemoryStorage())
    private val sessionEvents = SessionEvents()
    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun expiredAccessToken_isRefreshedAndRequestRetried() = runTest {
        tokenStore.start(AuthTokens("old-access", "refresh-1"), persistForMillis = 60_000)
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.endsWith(ApiEndpoints.REFRESH) ->
                    respond("""{"accessToken":"new-access","refreshToken":"refresh-2"}""", HttpStatusCode.OK, json)
                request.headers[HttpHeaders.Authorization] == "Bearer new-access" ->
                    respond("""{}""", HttpStatusCode.OK, json)
                else -> respondError(HttpStatusCode.Unauthorized, headers = headersOf(HttpHeaders.WWWAuthenticate, "Bearer"))
            }
        }
        val client = HttpClientFactory(config, tokenStore, sessionEvents).create(engine)

        client.get(ApiEndpoints.ME)

        assertEquals(AuthTokens("new-access", "refresh-2"), tokenStore.read())
    }

    @Test
    fun rejectedRefreshToken_wipesTokensAndEmitsExpired() = runTest {
        tokenStore.start(AuthTokens("old-access", "dead-refresh"), persistForMillis = 60_000)
        val engine = MockEngine { _ ->
            respondError(HttpStatusCode.Unauthorized, headers = headersOf(HttpHeaders.WWWAuthenticate, "Bearer"))
        }
        val client = HttpClientFactory(config, tokenStore, sessionEvents).create(engine)

        sessionEvents.events.test {
            safeApiCall { client.get(ApiEndpoints.ME) }
            assertEquals(SessionEvent.Expired, awaitItem())
        }
        assertNull(tokenStore.read())
    }
}
