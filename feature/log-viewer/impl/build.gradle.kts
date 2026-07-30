import ru.vladislavsumin.utils.vsCoreLibs

plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.adb.client)
            implementation(projects.core.boyerMooreSearch)
            implementation(projects.core.searchUtils)
            implementation(projects.core.ui.hint)
            implementation(projects.core.ui.selection)
            implementation(projects.core.ui.textHighlight)
            implementation(projects.feature.logRecent.api)
            implementation(projects.feature.logParser.api)
            implementation(projects.feature.bottomBar.api)
            implementation(projects.feature.notifications.api)
            implementation(projects.feature.tabs.api)
            implementation(projects.core.proguardParser)
            implementation(libs.betterParse)
            implementation(libs.backdrop)
            implementation(libs.google.datastore.core)
            implementation(libs.google.datastore.preferences)
            implementation(vsCoreLibs.kotlin.serialization.json)
            implementation(vsCoreLibs.vs.core.uikit.pieChart)
        }
        commonTest.dependencies {
            implementation(projects.feature.logParser.anime)
            implementation(vsCoreLibs.vs.core.logger.manager)
            implementation(vsCoreLibs.vs.core.navigation.test)
            implementation(vsCoreLibs.kotlin.coroutines.test)
            // TODO вынести в общий код
            implementation(kotlin("test"))
        }
    }
}
