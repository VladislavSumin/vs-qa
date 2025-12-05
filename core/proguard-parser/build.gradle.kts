plugins {
    id("ru.vladislavsumin.convention.preset.core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(vsCoreLibs.vs.core.logger.api)
            implementation(libs.r8)
        }
    }
}
