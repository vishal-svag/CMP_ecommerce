package ai.reslt.kmpready.core.security

/**
 * Encrypted key-value storage for secrets (tokens). Backed by:
 *  - Android: AES-256-GCM key in the hardware-backed Android Keystore, ciphertext in private prefs.
 *  - iOS: Keychain, `AfterFirstUnlockThisDeviceOnly` (never synced to iCloud / restored to other devices).
 *
 * Do NOT use this for large or non-sensitive data — use DataStore / a database instead.
 */
interface SecureStorage {
    suspend fun get(key: String): String?
    suspend fun put(key: String, value: String)
    suspend fun remove(key: String)
    suspend fun clear()
}

class SecureStorageException(message: String, cause: Throwable? = null) : Exception(message, cause)
