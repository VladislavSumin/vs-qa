plugins {
    id("ru.vladislavsumin.convention.preset.core")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.network)
            implementation(vsCoreLibs.vs.core.coroutines.dispatcher)
            implementation(vsCoreLibs.vs.core.di)
        }
    }
}
