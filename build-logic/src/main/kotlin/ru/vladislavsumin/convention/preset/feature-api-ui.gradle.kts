package ru.vladislavsumin.convention.preset

import ru.vladislavsumin.utils.vsCoreLibs

plugins {
    id("ru.vladislavsumin.convention.preset.feature-api")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(vsCoreLibs.vs.core.decompose.components)
            implementation(vsCoreLibs.vs.core.decompose.compose)
        }
    }
}
