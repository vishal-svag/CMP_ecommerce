package ai.reslt.kmpready.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { IllegalStateException("Library '$alias' missing from libs.versions.toml") }

internal fun KotlinMultiplatformExtension.commonMainDependencies(block: KotlinDependencyHandler.() -> Unit) {
    sourceSets.getByName("commonMain").dependencies(block)
}

internal fun KotlinMultiplatformExtension.commonTestDependencies(block: KotlinDependencyHandler.() -> Unit) {
    sourceSets.getByName("commonTest").dependencies(block)
}
