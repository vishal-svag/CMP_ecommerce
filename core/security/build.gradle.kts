plugins {
    alias(libs.plugins.kmpready.kmp.library)
}

kotlin {
    android {
        namespace = "ai.reslt.kmpready.core.security"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.biometric)
            implementation(libs.androidx.fragment)
            implementation(libs.androidx.core.ktx)
        }
        iosMain.dependencies {
            implementation(libs.multiplatform.settings)
        }
    }
}
