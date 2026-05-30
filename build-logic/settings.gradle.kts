val rootProps = java.util.Properties()
file("../gradle.properties").inputStream().use { rootProps.load(it) }
rootProps.forEach { (k, v) ->
    val key = k.toString()
    if (!extra.has(key)) extra.set(key, v.toString())
}

gradle.beforeProject {
    settings.extra.properties.forEach { (k, v) ->
        if (!project.hasProperty(k as String)) project.extensions.extraProperties.set(k, v)
    }
}

val useVsCoreSources = extra["ru.vs.core.useVsCoreSources"].toString().toBoolean()
if (useVsCoreSources) {
    includeBuild("../../vs-core/build-scripts")
}

apply { from("common-settings.gradle.kts") }
rootProject.name = "build-logic"
