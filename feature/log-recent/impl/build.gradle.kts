plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl-ui")
    id("ru.vladislavsumin.convention.room")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.ui.hint)
            implementation(projects.feature.notifications.api)
        }
    }
}
