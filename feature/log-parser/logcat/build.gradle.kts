import ru.vladislavsumin.utils.vsCoreLibs

plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.feature.logParser.api)
        }
        commonTest.dependencies {
            implementation(vsCoreLibs.vs.core.logger.manager)
            implementation(vsCoreLibs.kotlin.coroutines.test)
            implementation(kotlin("test"))
        }
    }
}
