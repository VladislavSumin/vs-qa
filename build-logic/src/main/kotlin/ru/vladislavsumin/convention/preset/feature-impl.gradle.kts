package ru.vladislavsumin.convention.preset

import ru.vladislavsumin.utils.vsCoreLibs

plugins {
    id("ru.vladislavsumin.convention.preset.feature-api")
    id("ru.vladislavsumin.convention.impl-to-api-dependency")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(vsCoreLibs.vs.core.di)
        }
    }
}
