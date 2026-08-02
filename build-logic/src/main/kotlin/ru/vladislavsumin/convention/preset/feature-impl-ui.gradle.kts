package ru.vladislavsumin.convention.preset

import ru.vladislavsumin.utils.libs
import ru.vladislavsumin.utils.vsCoreLibs

plugins {
    id("ru.vladislavsumin.convention.preset.feature-api-ui")
    id("ru.vladislavsumin.convention.preset.feature-impl")
    id("ru.vladislavsumin.convention.compose")
    id("ru.vladislavsumin.convention.compose-resources")
    id("ru.vladislavsumin.convention.compose-stability-analyzer")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:ui:hotkey-controller"))
            implementation(project(":core:ui:drag-and-drop"))
            implementation(project(":core:ui:file-picker"))
            implementation(project(":core:ui:design-system"))
            implementation(project(":core:ui:icons"))

            implementation(vsCoreLibs.vs.core.navigation.impl)
            implementation(vsCoreLibs.vs.core.navigation.di)
            implementation(vsCoreLibs.vs.core.navigation.factoryGenerator.api)

            implementation(vsCoreLibs.decompose.extensions.compose)

            implementation(libs.backdrop)

            implementation(compose.material3)
        }
    }
}

dependencies {
    add("kspJvm", vsCoreLibs.vs.core.navigation.factoryGenerator.ksp)
}
