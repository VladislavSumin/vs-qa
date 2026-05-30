/**
 * Общая для проекта и build-logc часть settings.gradle.kts
 */
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("vsCoreLibs") {
            val useVsCoreSources = extra["ru.vs.core.useVsCoreSources"].toString().toBoolean()
            if (useVsCoreSources) {
                from(files("../../vs-core/libs.versions.toml"))
            } else {
                from("ru.vladislavsumin:vs-core:${extra["ru.vs.core.version"]}")
            }
        }
        create("libs") {
            from(files("../libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    val useVsCoreSources = extra["ru.vs.core.useVsCoreSources"].toString().toBoolean()
    if (!useVsCoreSources) {
        resolutionStrategy {
            eachPlugin {
                if (requested.id.id.startsWith("ru.vladislavsumin.convention.")) {
                    useVersion(extra["ru.vs.core.version"].toString())
                }
            }
        }
    }
}
