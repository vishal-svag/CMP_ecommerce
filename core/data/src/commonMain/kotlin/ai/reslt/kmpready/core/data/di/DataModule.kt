package ai.reslt.kmpready.core.data.di

import ai.reslt.kmpready.core.data.remote.api.AuthApi
import ai.reslt.kmpready.core.data.remote.api.ProductApi
import ai.reslt.kmpready.core.data.repository.AuthRepositoryImpl
import ai.reslt.kmpready.core.data.repository.ProductRepositoryImpl
import ai.reslt.kmpready.core.data.verification.DevSecondFactorService
import ai.reslt.kmpready.core.data.verification.SecondFactorService
import ai.reslt.kmpready.core.domain.repository.AuthRepository
import ai.reslt.kmpready.core.domain.repository.ProductRepository
import ai.reslt.kmpready.core.network.clearCachedAuthTokens
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Qualifier for the app-lifetime CoroutineScope provided by the composition root. */
val AppScopeQualifier = named("AppScope")

val dataModule = module {
    single { AuthApi(get()) }
    single { ProductApi(get()) }
    // TODO(backend): swap for RemoteSecondFactorService once the 2FA API exists.
    single<SecondFactorService> { DevSecondFactorService(clock = get()) }

    single<AuthRepository> {
        val client = get<HttpClient>()
        AuthRepositoryImpl(
            api = get(),
            tokenStore = get(),
            secondFactor = get(),
            onTokensChanged = { client.clearCachedAuthTokens() },
            sessionEvents = get(),
            appScope = get(AppScopeQualifier),
            dispatchers = get(),
        )
    }
    single<ProductRepository> { ProductRepositoryImpl(get(), get()) }
}
