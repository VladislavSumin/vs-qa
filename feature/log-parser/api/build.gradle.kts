plugins {
    id("ru.vladislavsumin.convention.preset.feature-api")
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
