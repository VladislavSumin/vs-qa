plugins {
    id("ru.vladislavsumin.convention.preset.core")
    id("ru.vladislavsumin.convention.compose")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.material3)
            // TODO вынести в core библиотеку
            implementation(libs.kotlin.serialization.yaml)
        }
    }
}
