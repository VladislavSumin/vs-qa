plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(vsCoreLibs.vs.core.logger.api)
            implementation("com.android.tools:r8:8.11.18")
        }
    }
}
