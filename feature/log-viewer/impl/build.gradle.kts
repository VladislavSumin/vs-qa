plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
    id("ru.vladislavsumin.convention.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.logViewer.api)

            // TODO временное решение
            implementation(projects.feature.memoryIndicator.api)

            implementation(projects.core.utils)
            implementation(projects.core.proguardParser)
            implementation(projects.core.ui.hotkeyController)
            implementation(projects.core.ui.designSystem)

            implementation(vsCoreLibs.vs.core.logger.api)
            implementation(vsCoreLibs.vs.core.di)
            implementation(vsCoreLibs.vs.core.decompose.components)
            implementation(vsCoreLibs.vs.core.decompose.compose)
            implementation(compose.materialIconsExtended)
            implementation(compose.material3)
            implementation(libs.betterParse)
        }
    }
}
