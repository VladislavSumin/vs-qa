plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // TODO временное решение.
            implementation(projects.feature.logParser.anime)

            implementation(projects.feature.logParser.api)
            implementation(projects.feature.bottomBar.api)
            implementation(projects.core.proguardParser)
            implementation(libs.betterParse)
        }
    }
}
