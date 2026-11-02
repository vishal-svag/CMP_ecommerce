package ai.reslt.kmpready.core.data.verification

import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.domain.model.VerificationFactor

internal data class IssuedChallenge(
    val id: String,
    val codeLength: Int,
    val expiresAtMillis: Long,
    val resendAvailableAtMillis: Long,
)

/**
 * Second-factor backend contract. In production this is a thin API client — the server owns
 * code generation, delivery (SMS/email/push), expiry, attempt limits and device-key checks.
 */
internal interface SecondFactorService {
    suspend fun issue(userId: Long): AppResult<IssuedChallenge>
    suspend fun resend(challengeId: String): AppResult<IssuedChallenge>
    suspend fun verify(challengeId: String, factor: VerificationFactor): AppResult<Unit>
    fun discard(challengeId: String)
}
