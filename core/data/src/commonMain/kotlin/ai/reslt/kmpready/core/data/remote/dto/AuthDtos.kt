package ai.reslt.kmpready.core.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class LoginRequestDto(
    val username: String,
    val password: String,
    val expiresInMins: Int = 30,
) {
    override fun toString(): String = "LoginRequestDto(username=$username, password=***)"
}

@Serializable
internal data class LoginResponseDto(
    val id: Long,
    val username: String,
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val image: String? = null,
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
internal data class UserDto(
    val id: Long,
    val username: String,
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val image: String? = null,
)
