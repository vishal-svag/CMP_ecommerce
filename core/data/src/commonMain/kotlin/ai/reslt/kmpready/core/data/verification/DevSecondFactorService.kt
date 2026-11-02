package ai.reslt.kmpready.core.data.verification

import ai.reslt.kmpready.core.common.error.AppError
import ai.reslt.kmpready.core.common.logging.AppLogger
import ai.reslt.kmpready.core.common.result.AppResult
import ai.reslt.kmpready.core.common.time.AppClock
import ai.reslt.kmpready.core.domain.model.VerificationFactor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

/**
 * ⚠️ DEVELOPMENT ONLY — simulates the server until the real 2FA endpoint exists.
 * The passcode is printed to Logcat / Xcode console under the tag "DevOtp".
 * It mirrors production rules so the UI is exercised realistically:
 * 5-minute expiry, 45 s resend cooldown, 5 attempts, 3 resends.
 *
 * Replace with a RemoteSecondFactorService (API client) before release.
 */
internal class DevSecondFactorService(
    private val clock: AppClock,
    private val random: Random = Random.Default,
) : SecondFactorService {

    private class Entry(var code: String, var expiresAt: Long, var resendAt: Long, var attempts: Int, var resends: Int)

    private val log = AppLogger.withTag("DevOtp")
    private val mutex = Mutex()
    private val entries = mutableMapOf<String, Entry>()

    override suspend fun issue(userId: Long): AppResult<IssuedChallenge> = mutex.withLock {
        val id = "chl_${userId}_${random.nextLong(Long.MAX_VALUE).toString(36)}"
        val now = clock.nowMillis()
        val entry = Entry(newCode(), now + TTL_MS, now + RESEND_COOLDOWN_MS, attempts = 0, resends = 0)
        entries[id] = entry
        announce(entry.code, reason = "new sign-in")
        AppResult.Success(entry.toIssued(id))
    }

    override suspend fun resend(challengeId: String): AppResult<IssuedChallenge> = mutex.withLock {
        val entry = entries[challengeId] ?: return@withLock AppResult.Failure(AppError.NoPendingVerification)
        val now = clock.nowMillis()
        when {
            now < entry.resendAt -> AppResult.Failure(AppError.TooManyRequests)
            entry.resends >= MAX_RESENDS -> AppResult.Failure(AppError.TooManyRequests)
            else -> {
                entry.code = newCode()
                entry.expiresAt = now + TTL_MS
                entry.resendAt = now + RESEND_COOLDOWN_MS
                entry.resends++
                announce(entry.code, reason = "resent — previous code is now invalid")
                AppResult.Success(entry.toIssued(challengeId))
            }
        }
    }

    override suspend fun verify(challengeId: String, factor: VerificationFactor): AppResult<Unit> = mutex.withLock {
        val entry = entries[challengeId] ?: return@withLock AppResult.Failure(AppError.NoPendingVerification)
        when (factor) {
            VerificationFactor.DeviceBiometric -> {
                log.w { "DEV: accepting local biometric as second factor. Production must verify a device-bound key." }
                entries.remove(challengeId)
                AppResult.Success(Unit)
            }
            is VerificationFactor.Passcode -> when {
                clock.nowMillis() >= entry.expiresAt -> AppResult.Failure(AppError.CodeExpired)
                constantTimeEquals(factor.code, entry.code) -> {
                    entries.remove(challengeId)
                    AppResult.Success(Unit)
                }
                else -> {
                    log.w { "DEV: passcode mismatch — use the LATEST 'DEV passcode' line" }
                    entry.attempts++
                    val remaining = MAX_ATTEMPTS - entry.attempts
                    if (remaining <= 0) {
                        entries.remove(challengeId)
                        AppResult.Failure(AppError.VerificationLocked)
                    } else {
                        AppResult.Failure(AppError.InvalidCode(remaining))
                    }
                }
            }
        }
    }

    override fun discard(challengeId: String) {
        // Best effort; called from non-suspending cancel paths.
        if (mutex.tryLock()) {
            try { entries.remove(challengeId) } finally { mutex.unlock() }
        }
    }

    private fun newCode(): String = (0 until CODE_LENGTH).joinToString("") { random.nextInt(10).toString() }

    private fun announce(code: String, reason: String) =
        log.w { "DEV passcode: $code  ($reason, valid ${TTL_MS / 60_000} min)" }

    private fun Entry.toIssued(id: String) = IssuedChallenge(id, CODE_LENGTH, expiresAt, resendAt)

    /** Avoids leaking how many leading digits matched via timing. */
    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) diff = diff or (a[i].code xor b[i].code)
        return diff == 0
    }

    companion object {
        const val CODE_LENGTH = 6
        const val TTL_MS = 5 * 60_000L
        const val RESEND_COOLDOWN_MS = 45_000L
        const val MAX_ATTEMPTS = 5
        const val MAX_RESENDS = 3
    }
}
