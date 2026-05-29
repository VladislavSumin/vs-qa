val useVsCoreSources = extra["ru.vs.core.useVsCoreSources"].toString().toBoolean()
if (useVsCoreSources) {
    includeBuild("../../vs-core/build-scripts")
}

apply { from("common-settings.gradle.kts") }
rootProject.name = "build-logic"
