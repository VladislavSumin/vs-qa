import ru.vladislavsumin.utils.vsCoreLibs

plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.logRecent.api)
            implementation(projects.feature.logParser.api)
            implementation(projects.feature.bottomBar.api)
            implementation(projects.feature.notifications.api)
            implementation(projects.feature.windowTitle.api)
            implementation(projects.core.proguardParser)
            implementation(libs.betterParse)
            implementation(libs.google.datastore.core)
            implementation(libs.google.datastore.preferences)
            implementation(vsCoreLibs.kotlin.serialization.json)
        }
        commonTest.dependencies {
            // TODO порешать с логером в тестах
            implementation(vsCoreLibs.vs.core.logger.manager)
            // TODO вынести в общий код
            implementation(kotlin("test"))
        }
    }
}
