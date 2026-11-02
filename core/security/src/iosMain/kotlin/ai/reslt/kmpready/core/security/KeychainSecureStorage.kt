package ai.reslt.kmpready.core.security

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFBridgingRetain
import platform.Foundation.NSUserDefaults
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrService

/**
 * Keychain-backed storage. Items are `AfterFirstUnlockThisDeviceOnly`: readable by background
 * work after first unlock, never synced to iCloud Keychain, never restored onto another device.
 */
@OptIn(ExperimentalSettingsImplementation::class, ExperimentalSettingsApi::class, ExperimentalForeignApi::class)
internal class KeychainSecureStorage(
    service: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SecureStorage {

    private val keychain = KeychainSettings(
        kSecAttrService!! to CFBridgingRetain(service)!!,
        kSecAttrAccessible!! to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly!!,
    )

    override suspend fun get(key: String): String? = withContext(dispatcher) {
        keychain.getStringOrNull(key)
    }

    override suspend fun put(key: String, value: String) = withContext(dispatcher) {
        keychain.putString(key, value)
    }

    override suspend fun remove(key: String) = withContext(dispatcher) {
        keychain.remove(key)
    }

    override suspend fun clear() = withContext(dispatcher) {
        keychain.clear()
    }

    /**
     * Keychain items survive app uninstall. On the first launch after a (re)install we wipe them,
     * otherwise a reinstalled app would silently resume the previous user's session.
     */
    fun wipeIfFreshInstall() {
        val defaults = NSUserDefaults.standardUserDefaults
        if (!defaults.boolForKey(FIRST_RUN_FLAG)) {
            keychain.clear()
            defaults.setBool(true, forKey = FIRST_RUN_FLAG)
        }
    }

    private companion object {
        const val FIRST_RUN_FLAG = "ai.reslt.kmpready.keychain_initialized"
    }
}
