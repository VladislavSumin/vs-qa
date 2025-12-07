package ru.vladislavsumin.convention.preset

import ru.vladislavsumin.utils.vsCoreLibs

plugins {
    id("ru.vladislavsumin.convention.preset.feature-api")
    id("ru.vladislavsumin.convention.impl-to-api-dependency")
    id("ru.vladislavsumin.convention.ksp-jvm-hack")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:utils"))
            implementation(vsCoreLibs.vs.core.di)
            implementation(vsCoreLibs.vs.core.fs.api)
            implementation(vsCoreLibs.vs.core.factoryGenerator.api)
        }
    }
}

dependencies {
    add("kspJvm", vsCoreLibs.vs.core.factoryGenerator.ksp)
}
