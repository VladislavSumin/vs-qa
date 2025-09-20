package ru.vladislavsumin.convention.preset

import ru.vladislavsumin.utils.vsCoreLibs

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

            implementation(vsCoreLibs.vs.core.navigation.impl)
            implementation(vsCoreLibs.vs.core.navigation.di)
            implementation(vsCoreLibs.vs.core.navigation.factoryGenerator.api)

            implementation(vsCoreLibs.decompose.extensions.compose)

            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
        }
    }
}

dependencies {
    add("kspJvm", vsCoreLibs.vs.core.navigation.factoryGenerator.ksp)
}
