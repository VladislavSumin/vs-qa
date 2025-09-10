package ru.vladislavsumin.convention.preset

plugins {
    id("ru.vladislavsumin.convention.preset.feature-api-ui")
    id("ru.vladislavsumin.convention.preset.feature-impl")
    id("ru.vladislavsumin.convention.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ui:hotkey-controller"))
            implementation(project(":core:ui:design-system"))

            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
        }
    }
}
