import ru.vladislavsumin.utils.vsCoreLibs

plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.logParser.api)
            implementation(projects.feature.logParser.generic)
        }
        commonTest.dependencies {
            implementation(vsCoreLibs.vs.core.logger.manager)
            implementation(kotlin("test"))
        }
    }
}
