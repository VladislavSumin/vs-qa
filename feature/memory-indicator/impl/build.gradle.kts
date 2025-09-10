plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
    id("ru.vladislavsumin.convention.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.memoryIndicator.api)
            implementation(vsCoreLibs.vs.core.di)
            implementation(vsCoreLibs.vs.core.decompose.components)
            implementation(vsCoreLibs.vs.core.decompose.compose)
            implementation(compose.material3)
        }
    }
}
