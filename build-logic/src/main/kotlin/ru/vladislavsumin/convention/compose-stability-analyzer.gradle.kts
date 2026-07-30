package ru.vladislavsumin.convention

import ru.vladislavsumin.configuration.qaProjectConfiguration
import ru.vladislavsumin.utils.libs

plugins {
    kotlin("multiplatform")
    id("com.github.skydoves.compose.stability.analyzer")
}

val config = qaProjectConfiguration.composeStabilityAnalyzer

composeStabilityAnalyzer {
    enabled.set(config.enabled)
    traceAll {
        enabled.set(config.traceAll)
        threshold.set(2)
        variants.set(listOf("debug"))
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.stability.runtime)
        }
    }
}
