package ai.reslt.kmpready.di

import ai.reslt.kmpready.app.AppViewModel
import ai.reslt.kmpready.core.common.config.AppConfig
import ai.reslt.kmpready.core.common.coroutines.DefaultDispatcherProvider
import ai.reslt.kmpready.core.common.coroutines.DispatcherProvider
import ai.reslt.kmpready.core.common.logging.AppLogger
import ai.reslt.kmpready.core.common.time.AppClock
import ai.reslt.kmpready.core.common.time.SystemAppClock
import ai.reslt.kmpready.core.data.di.AppScopeQualifier
import ai.reslt.kmpready.core.data.di.dataModule
import ai.reslt.kmpready.core.domain.usecase.CancelLoginChallengeUseCase
import ai.reslt.kmpready.core.domain.usecase.ContinueAsGuestUseCase
import ai.reslt.kmpready.core.domain.usecase.ExternalSignInUseCase
import ai.reslt.kmpready.core.domain.usecase.GetCurrentUserUseCase
import ai.reslt.kmpready.core.domain.usecase.GetProductsUseCase
import ai.reslt.kmpready.core.domain.usecase.LoginUseCase
import ai.reslt.kmpready.core.domain.usecase.LogoutUseCase
import ai.reslt.kmpready.core.domain.usecase.ObserveSessionUseCase
import ai.reslt.kmpready.core.domain.usecase.ObservePendingChallengeUseCase
import ai.reslt.kmpready.core.domain.usecase.ResendCodeUseCase
import ai.reslt.kmpready.core.domain.usecase.RestoreSessionUseCase
import ai.reslt.kmpready.core.domain.usecase.VerifyLoginUseCase
import ai.reslt.kmpready.core.network.di.networkModule
import ai.reslt.kmpready.core.security.di.securityModule
import ai.reslt.kmpready.feature.auth.di.authFeatureModule
import ai.reslt.kmpready.feature.home.di.homeFeatureModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

private fun coreModule(config: AppConfig) = module {
    single { config }
    single<DispatcherProvider> { DefaultDispatcherProvider }
    single<AppClock> { SystemAppClock }
    single<CoroutineScope>(AppScopeQualifier) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
}

private val domainModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::ContinueAsGuestUseCase)
    factoryOf(::ObserveSessionUseCase)
    factoryOf(::RestoreSessionUseCase)
    factoryOf(::GetCurrentUserUseCase)
    factoryOf(::GetProductsUseCase)
    factoryOf(::ObservePendingChallengeUseCase)
    factoryOf(::VerifyLoginUseCase)
    factoryOf(::ResendCodeUseCase)
    factoryOf(::CancelLoginChallengeUseCase)
    factoryOf(::ExternalSignInUseCase)
}

private val appModule = module {
    viewModelOf(::AppViewModel)
}

fun appModules(config: AppConfig): List<Module> = listOf(
    coreModule(config),
    securityModule,
    networkModule,
    dataModule,
    domainModule,
    authFeatureModule,
    homeFeatureModule,
    appModule,
)

/**
 * Single entry point for both platforms. Android passes `androidContext(...)` via [platform].
 * Safe to call once per process.
 */
fun initKoin(config: AppConfig, platform: KoinAppDeclaration = {}): KoinApplication {
    AppLogger.configure(config.isDebug)
    return startKoin {
        platform()
        modules(appModules(config))
    }
}
