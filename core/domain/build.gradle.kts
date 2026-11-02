plugins {
    alias(libs.plugins.kmpready.kmp.library)
}

// Pure Kotlin: no Compose, no Ktor, no Koin, no platform code. Keep it that way.
kotlin {
    android {
        namespace = "ai.reslt.kmpready.core.domain"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
        }
    }
}
