plugins {
    alias(libs.plugins.kmpready.kmp.library)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    android {
        namespace = "ai.reslt.kmpready.core.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:network"))
            implementation(project(":core:security"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
        }
    }
}
