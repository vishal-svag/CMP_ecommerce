package ai.reslt.kmpready.core.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * AES-256-GCM with a non-exportable key in the Android Keystore (TEE / StrongBox-backed
 * where available). Only ciphertext ever touches disk.
 *
 * Replaces the deprecated androidx.security EncryptedSharedPreferences.
 */
internal class AndroidKeystoreSecureStorage(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : SecureStorage {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mutex = Mutex()

    override suspend fun get(key: String): String? = withContext(dispatcher) {
        mutex.withLock {
            val encoded = prefs.getString(key, null) ?: return@withLock null
            try {
                decrypt(encoded)
            } catch (e: GeneralSecurityException) {
                // Key invalidated (e.g. lock-screen removed) or data tampered: drop the value.
                prefs.edit().remove(key).commit()
                null
            } catch (e: IllegalArgumentException) {
                prefs.edit().remove(key).commit()
                null
            }
        }
    }

    override suspend fun put(key: String, value: String) = withContext(dispatcher) {
        mutex.withLock {
            val encrypted = try {
                encrypt(value)
            } catch (e: GeneralSecurityException) {
                throw SecureStorageException("Encryption failed", e)
            }
            if (!prefs.edit().putString(key, encrypted).commit()) {
                throw SecureStorageException("Failed to persist '$key'")
            }
        }
    }

    override suspend fun remove(key: String) = withContext(dispatcher) {
        mutex.withLock { prefs.edit().remove(key).commit(); Unit }
    }

    override suspend fun clear() = withContext(dispatcher) {
        mutex.withLock { prefs.edit().clear().commit(); Unit }
    }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val iv = cipher.iv
        val cipherText = cipher.doFinal(plain.encodeToByteArray())
        return Base64.encodeToString(iv + cipherText, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): String {
        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        require(bytes.size > IV_SIZE) { "Corrupted payload" }
        val iv = bytes.copyOfRange(0, IV_SIZE)
        val cipherText = bytes.copyOfRange(IV_SIZE, bytes.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_SIZE_BITS, iv))
        return cipher.doFinal(cipherText).decodeToString()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE_BITS)
            .setRandomizedEncryptionRequired(true)
            .build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply { init(spec) }
            .generateKey()
    }

    private companion object {
        const val PREFS_NAME = "secure_storage"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "ai.reslt.kmpready.secure_storage.v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_SIZE_BITS = 256
        const val TAG_SIZE_BITS = 128
        const val IV_SIZE = 12
    }
}
