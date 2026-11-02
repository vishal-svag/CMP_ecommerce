package ai.reslt.kmpready.core.network.di

import ai.reslt.kmpready.core.network.HttpClientFactory
import ai.reslt.kmpready.core.network.auth.SessionEvents
import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.koin.dsl.onClose

/** Requires an [ai.reslt.kmpready.core.common.config.AppConfig] and the security module. */
val networkModule = module {
    single { SessionEvents() }
    single { HttpClientFactory(config = get(), tokenStore = get(), sessionEvents = get()) }
    single<HttpClient> { get<HttpClientFactory>().create() } onClose { it?.close() }
}
