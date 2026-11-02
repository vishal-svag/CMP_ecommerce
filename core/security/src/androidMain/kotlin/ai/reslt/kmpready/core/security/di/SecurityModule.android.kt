package ai.reslt.kmpready.core.security.di

import ai.reslt.kmpready.core.common.biometric.BiometricAuthenticator
import ai.reslt.kmpready.core.security.AndroidBiometricAuthenticator
import ai.reslt.kmpready.core.security.AndroidKeystoreSecureStorage
import ai.reslt.kmpready.core.security.CurrentActivityTracker
import ai.reslt.kmpready.core.security.SecureStorage
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformSecurityModule: Module = module {
    single<SecureStorage> { AndroidKeystoreSecureStorage(androidContext()) }
    single(createdAtStart = true) { CurrentActivityTracker(androidContext() as Application) }
    single<BiometricAuthenticator> { AndroidBiometricAuthenticator(androidContext(), get()) }
}
