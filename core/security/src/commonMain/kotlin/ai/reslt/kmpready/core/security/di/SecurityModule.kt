package ai.reslt.kmpready.core.security.di

import ai.reslt.kmpready.core.security.TokenStore
import org.koin.core.module.Module
import org.koin.dsl.module

/** Provides the platform SecureStorage and BiometricAuthenticator. */
internal expect val platformSecurityModule: Module

val securityModule: Module = module {
    includes(platformSecurityModule)
    single { TokenStore(storage = get(), clock = get()) }
}
