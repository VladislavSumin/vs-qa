import ru.vladislavsumin.utils.vsCoreLibs

plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.logViewer.api)
            implementation(libs.mcp.kotlin.sdk.server)
            implementation(vsCoreLibs.kotlin.serialization.json)
        }
        jvmMain.dependencies {
            implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.26.0")
        }
    }
}
