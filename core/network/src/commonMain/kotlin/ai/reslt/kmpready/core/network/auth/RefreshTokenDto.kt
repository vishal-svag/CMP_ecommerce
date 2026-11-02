package ai.reslt.kmpready.core.network.auth

import kotlinx.serialization.Serializable

@Serializable
internal data class RefreshTokenRequestDto(
    val refreshToken: String,
    val expiresInMins: Int = ACCESS_TOKEN_TTL_MINUTES,
)

@Serializable
internal data class RefreshTokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
)

internal const val ACCESS_TOKEN_TTL_MINUTES = 30
