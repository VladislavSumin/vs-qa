plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl-ui")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(vsCoreLibs.vs.core.serialization.yaml)

                implementation(projects.feature.rootScreen.api)
                implementation(projects.feature.logViewer.api)

                implementation(projects.feature.windowTitle.impl)
            }
        }
    }
}
