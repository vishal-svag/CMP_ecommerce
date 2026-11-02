package ai.reslt.kmpready.core.data.remote.api

import ai.reslt.kmpready.core.data.remote.dto.LoginRequestDto
import ai.reslt.kmpready.core.data.remote.dto.LoginResponseDto
import ai.reslt.kmpready.core.data.remote.dto.UserDto
import ai.reslt.kmpready.core.network.ApiEndpoints
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

internal class AuthApi(private val client: HttpClient) {

    suspend fun login(request: LoginRequestDto): LoginResponseDto =
        client.post(ApiEndpoints.LOGIN) { setBody(request) }.body()

    suspend fun me(): UserDto = client.get(ApiEndpoints.ME).body()
}
