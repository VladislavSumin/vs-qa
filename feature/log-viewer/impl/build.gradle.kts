plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl-ui")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // TODO временное решение
            implementation(projects.feature.memoryIndicator.api)

            implementation(projects.core.proguardParser)
            implementation(libs.betterParse)
        }
    }
}
