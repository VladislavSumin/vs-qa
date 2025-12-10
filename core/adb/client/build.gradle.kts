plugins {
    id("ru.vladislavsumin.convention.preset.core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.network)
        }
    }
}
