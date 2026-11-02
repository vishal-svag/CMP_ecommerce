package ai.reslt.kmpready.core.security

import ai.reslt.kmpready.core.common.time.AppClock
import ai.reslt.kmpready.core.common.time.SystemAppClock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
) {
    override fun toString(): String = "AuthTokens(***)"
}

/**
 * Session tokens, in one of two modes:
 *  - **Ephemeral** (default): memory only — gone when the process dies.
 *  - **Persisted** ("keep me authenticated"): encrypted in [SecureStorage] with a hard expiry,
 *    after which they are wiped on read regardless of server-side TTL.
 *
 * All operations are serialized so a token refresh can never interleave with logout and
 * resurrect a dead session.
 */
class TokenStore(
    private val storage: SecureStorage,
    private val clock: AppClock = SystemAppClock,
) {
    private val mutex = Mutex()
    private var cached: AuthTokens? = null
    private var persisted = false
    private var loadedFromDisk = false

    suspend fun read(): AuthTokens? = mutex.withLock {
        cached?.let { return@withLock it }
        if (loadedFromDisk) return@withLock null
        loadedFromDisk = true

        val access = storage.get(KEY_ACCESS)
        val refresh = storage.get(KEY_REFRESH)
        val expiresAt = storage.get(KEY_EXPIRES_AT)?.toLongOrNull()
        if (access == null || refresh == null || expiresAt == null || clock.nowMillis() >= expiresAt) {
            wipeDisk()
            return@withLock null
        }
        persisted = true
        AuthTokens(access, refresh).also { cached = it }
    }

    /** Starts a new session. [persistForMillis] null = ephemeral. */
    suspend fun start(tokens: AuthTokens, persistForMillis: Long?) = mutex.withLock {
        wipeDisk()
        cached = tokens
        persisted = persistForMillis != null
        loadedFromDisk = true
        if (persistForMillis != null) {
            storage.put(KEY_ACCESS, tokens.accessToken)
            storage.put(KEY_REFRESH, tokens.refreshToken)
            storage.put(KEY_EXPIRES_AT, (clock.nowMillis() + persistForMillis).toString())
        }
    }

    /** Token refresh: keeps the current mode and the original expiry (refresh never extends "remember me"). */
    suspend fun update(tokens: AuthTokens) = mutex.withLock {
        cached = tokens
        if (persisted) {
            storage.put(KEY_ACCESS, tokens.accessToken)
            storage.put(KEY_REFRESH, tokens.refreshToken)
        }
    }

    suspend fun clear() = mutex.withLock {
        cached = null
        persisted = false
        loadedFromDisk = true
        wipeDisk()
    }

    private suspend fun wipeDisk() {
        storage.remove(KEY_ACCESS)
        storage.remove(KEY_REFRESH)
        storage.remove(KEY_EXPIRES_AT)
    }

    private companion object {
        const val KEY_ACCESS = "auth.access_token"
        const val KEY_REFRESH = "auth.refresh_token"
        const val KEY_EXPIRES_AT = "auth.session_expires_at"
    }
}
