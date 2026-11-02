package ai.reslt.kmpready.feature.auth.di

import ai.reslt.kmpready.feature.auth.login.LoginViewModel
import ai.reslt.kmpready.feature.auth.verify.VerifyViewModel
import ai.reslt.kmpready.feature.auth.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Explicit factories (named arguments) instead of `viewModelOf(::X)`:
 * the constructor-reference overloads give an unhelpful "none applicable" error when inference
 * fails, while this form compiles predictably and pinpoints any missing dependency.
 */
val authFeatureModule = module {
    viewModel { WelcomeViewModel(continueAsGuest = get()) }
    viewModel {
        LoginViewModel(
            login = get(),
            externalSignIn = get(),
            biometrics = get(),
            config = get(),
        )
    }
    viewModel {
        VerifyViewModel(
            observeChallenge = get(),
            verify = get(),
            resend = get(),
            cancelChallenge = get(),
            biometrics = get(),
            clock = get(),
            config = get(),
        )
    }
}
