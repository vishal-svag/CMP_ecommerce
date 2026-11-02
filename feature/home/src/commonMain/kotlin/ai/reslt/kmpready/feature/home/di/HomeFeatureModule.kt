package ai.reslt.kmpready.feature.home.di

import ai.reslt.kmpready.feature.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeFeatureModule = module {
    viewModel {
        HomeViewModel(
            getProducts = get(),
            getCurrentUser = get(),
            logout = get(),
            observeSession = get(),
        )
    }
}
