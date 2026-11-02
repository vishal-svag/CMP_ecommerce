plugins {
    `kotlin-dsl`
}

group = "ai.reslt.kmpready.buildlogic"

dependencies {
    // compileOnly: the real plugin versions come from the root build (plugins { alias(...) apply false }),
    // which avoids classpath conflicts between the included build and the main build.
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "kmpready.kmp.library"
            implementationClass = "ai.reslt.kmpready.buildlogic.KmpLibraryConventionPlugin"
        }
        register("kmpCompose") {
            id = "kmpready.kmp.compose"
            implementationClass = "ai.reslt.kmpready.buildlogic.KmpComposeConventionPlugin"
        }
        register("kmpFeature") {
            id = "kmpready.kmp.feature"
            implementationClass = "ai.reslt.kmpready.buildlogic.KmpFeatureConventionPlugin"
        }
    }
}
