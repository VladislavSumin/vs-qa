plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
    id("ru.vladislavsumin.convention.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.material3)
        }
    }
}
