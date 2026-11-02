plugins {
    alias(libs.plugins.kmpready.kmp.library)
}

kotlin {
    android {
        namespace = "ai.reslt.kmpready.core.common"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kermit)
        }
    }
}
