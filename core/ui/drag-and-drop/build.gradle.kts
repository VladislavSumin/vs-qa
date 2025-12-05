plugins {
    id("ru.vladislavsumin.convention.preset.core")
    id("ru.vladislavsumin.convention.compose")
}

kotlin {
    sourceSets{
        commonMain.dependencies {
            implementation(compose.foundation)
        }
    }
}
