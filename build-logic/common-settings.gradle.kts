/**
 * Общая для проекта и build-logc часть settings.gradle.kts
 */

apply { from("../../vs-core/build-scripts/common-settings.gradle.kts") }

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../libs.versions.toml"))
        }
    }
}
