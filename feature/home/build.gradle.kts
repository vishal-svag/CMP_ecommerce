plugins {
    alias(libs.plugins.kmpready.kmp.feature)
}

kotlin {
    android {
        namespace = "ai.reslt.kmpready.feature.home"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources { enable = true }
        withHostTest {}
    }
}

compose.resources {
    packageOfResClass = "ai.reslt.kmpready.feature.home.resources"
    publicResClass = false
}
