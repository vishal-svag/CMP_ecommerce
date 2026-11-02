package ai.reslt.kmpready.core.network

import ai.reslt.kmpready.core.common.config.AppConfig
import ai.reslt.kmpready.core.common.logging.AppLogger
import ai.reslt.kmpready.core.network.auth.RefreshTokenRequestDto
import ai.reslt.kmpready.core.network.auth.RefreshTokenResponseDto
import ai.reslt.kmpready.core.network.auth.SessionEvent
import ai.reslt.kmpready.core.network.auth.SessionEvents
import ai.reslt.kmpready.core.security.AuthTokens
import ai.reslt.kmpready.core.security.TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import io.ktor.client.plugins.logging.Logger as KtorLogger

class HttpClientFactory(
    private val config: AppConfig,
    private val tokenStore: TokenStore,
    private val sessionEvents: SessionEvents,
) {
    private val httpLog = AppLogger.withTag("Http")
    private val apiHost = Url(config.baseUrl).host

    fun create(engine: HttpClientEngine = createPlatformEngine(config.certificatePins)): HttpClient =
        HttpClient(engine) {
            expectSuccess = true

            defaultRequest {
                url(config.baseUrl)
                contentType(ContentType.Application.Json)
            }

            install(ContentNegotiation) { json(NetworkJson) }

            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }

            // Only idempotent GETs are retried — never replay a login / checkout POST.
            install(HttpRequestRetry) {
                retryIf(maxRetries = 2) { request, response ->
                    request.method == HttpMethod.Get && response.status.value in 500..599
                }
                retryOnExceptionIf(maxRetries = 2) { request, cause ->
                    request.method == HttpMethod.Get && cause !is CancellationException
                }
                exponentialDelay()
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        tokenStore.read()?.let { BearerTokens(it.accessToken, it.refreshToken) }
                    }
                    refreshTokens {
                        val refreshToken = oldTokens?.refreshToken ?: return@refreshTokens null
                        try {
                            val response: RefreshTokenResponseDto = client.post(ApiEndpoints.REFRESH) {
                                markAsRefreshTokenRequest()
                                setBody(RefreshTokenRequestDto(refreshToken))
                            }.body()
                            tokenStore.update(AuthTokens(response.accessToken, response.refreshToken))
                            BearerTokens(response.accessToken, response.refreshToken)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: ClientRequestException) {
                            // Refresh token rejected → session is dead. Network blips do NOT log the user out.
                            httpLog.w { "Refresh rejected (${e.response.status.value}); ending session" }
                            tokenStore.clear()
                            sessionEvents.emit(SessionEvent.Expired)
                            null
                        } catch (e: Exception) {
                            httpLog.w(e) { "Refresh failed transiently" }
                            null
                        }
                    }
                    // Attach the token up-front, but only to our own API host — never leak it to 3rd parties.
                    sendWithoutRequest { request -> request.url.host == apiHost }
                }
            }

            if (config.isDebug) {
                install(Logging) {
                    level = LogLevel.HEADERS // never BODY: bodies contain passwords / PII
                    logger = object : KtorLogger {
                        override fun log(message: String) = httpLog.d { message }
                    }
                    sanitizeHeader { header -> header == HttpHeaders.Authorization || header == HttpHeaders.Cookie }
                }
            }
        }
}

/** Drops Ktor's in-memory bearer cache so the next request re-reads [TokenStore]. Call after login/logout. */
fun HttpClient.clearCachedAuthTokens() {
    authProvider<BearerAuthProvider>()?.clearToken()
}
