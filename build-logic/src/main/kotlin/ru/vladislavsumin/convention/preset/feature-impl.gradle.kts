package ru.vladislavsumin.convention.preset

import ru.vladislavsumin.utils.vsCoreLibs

plugins {
    id("ru.vladislavsumin.convention.preset.feature-api")
    id("ru.vladislavsumin.convention.impl-to-api-dependency")
    id("ru.vladislavsumin.convention.kmp.ksp-hack")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:utils"))
            implementation(vsCoreLibs.vs.core.di)
            implementation(vsCoreLibs.vs.core.factoryGenerator.api)
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", vsCoreLibs.vs.core.factoryGenerator.ksp)
}
