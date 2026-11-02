package ai.reslt.kmpready.core.security.di

import ai.reslt.kmpready.core.common.biometric.BiometricAuthenticator
import ai.reslt.kmpready.core.security.IosBiometricAuthenticator
import ai.reslt.kmpready.core.security.KeychainSecureStorage
import ai.reslt.kmpready.core.security.SecureStorage
import org.koin.core.module.Module
import org.koin.dsl.module

private const val KEYCHAIN_SERVICE = "ai.reslt.kmpready.secure"

internal actual val platformSecurityModule: Module = module {
    single<SecureStorage> {
        KeychainSecureStorage(KEYCHAIN_SERVICE).also { it.wipeIfFreshInstall() }
    }
    single<BiometricAuthenticator> { IosBiometricAuthenticator() }
}
