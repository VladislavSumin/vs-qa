package ru.vladislavsumin.convention

import ru.vladislavsumin.utils.libs

plugins {
    kotlin("multiplatform")
    id("com.github.skydoves.compose.stability.analyzer")
}

composeStabilityAnalyzer {
    enabled.set(false)
    traceAll {
        enabled.set(true)
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
