plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(vsCoreLibs.vs.core.decompose.components)
            implementation(vsCoreLibs.vs.core.decompose.compose)
        }
    }
}
