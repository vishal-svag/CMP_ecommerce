package ai.reslt.kmpready.core.network

import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.result.AppResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class SafeApiCallTest {

    @Serializable
    data class Dto(val id: Int)

    private fun client(status: HttpStatusCode, body: String = "{}") = HttpClient(MockEngine) {
        expectSuccess = true
        install(ContentNegotiation) { json(NetworkJson) }
        engine {
            addHandler {
                respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
            }
        }
    }

    @Test
    fun success_isWrapped() = runTest {
        val result = safeApiCall { client(HttpStatusCode.OK, """{"id":7,"extra":"ignored"}""").get("x").body<Dto>() }
        assertEquals(AppResult.Success(Dto(7)), result)
    }

    @Test
    fun http401_mapsToUnauthorized() = runTest {
        val result = safeApiCall { client(HttpStatusCode.Unauthorized).get("x").body<Dto>() }
        assertEquals(AppResult.Failure(AppError.Unauthorized), result)
    }

    @Test
    fun http503_mapsToServer() = runTest {
        val result = safeApiCall { client(HttpStatusCode.ServiceUnavailable).get("x").body<Dto>() }
        assertEquals(AppResult.Failure(AppError.Server(503)), result)
    }

    @Test
    fun malformedJson_mapsToParsing() = runTest {
        val result = safeApiCall { client(HttpStatusCode.OK, """{"id":"oops"}""").get("x").body<Dto>() }
        assertEquals(AppResult.Failure(AppError.Parsing), result)
    }
}
