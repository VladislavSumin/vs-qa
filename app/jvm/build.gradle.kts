import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import proguard.gradle.ProGuardTask
import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.utils.fatJar
import java.io.File

plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
    id("ru.vladislavsumin.convention.compose")
    id("com.codingfeline.buildkonfig")
    id("io.sentry.kotlin.multiplatform.gradle")
}

kotlin {
    val mainClassName = "ru.vladislavsumin.qa.MainKt"
    jvm {
        fatJar(
            mainClass = mainClassName,
            jarName = "vs-qa",
            duplicatesStrategy = DuplicatesStrategy.WARN,
        ).configure {
            exclude("libskiko-macos-x64.*")
        }
        mainRun {
            mainClass.set(mainClassName)
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.app.core)
        }

        jvmMain.dependencies {
            implementation(projects.core.ui.hotkeyController)
            implementation(projects.feature.rootScreen.impl)
            implementation(projects.feature.windowTitle.impl)

            implementation(vsCoreLibs.vs.core.decompose.compose)
            implementation(vsCoreLibs.vs.core.di)
            implementation(vsCoreLibs.vs.core.logger.api)
            implementation(vsCoreLibs.vs.core.navigation.impl)

            implementation(vsCoreLibs.decompose.core)
            implementation(vsCoreLibs.decompose.extensions.compose)
            implementation(vsCoreLibs.kodein.core)
        }
    }
}

buildkonfig {
    packageName = "ru.vladislavsumin.qa"
    objectName = "BuildConfig"

    defaultConfigs {
        buildConfigField(STRING, "version", project.projectConfiguration.version)
    }
}

tasks.register<ProGuardTask>("buildFatJarMainMin") {
    dependsOn("buildFatJarMain")
    injars("build/libs/vs-qa.jar")
    outjars("build/libs/vs-qa-min.jar")

    configuration("proguard-rules.pro")
    printconfiguration("build/reports/proguard/proguard.pro")

    File("${System.getProperty("java.home")}/jmods/").listFiles().forEach { file ->
        libraryjars(
            mapOf(
                "jarfilter" to "!**.jar",
                "filter" to "!module-info.class",
            ),
            file.absolutePath,
        )
    }
}
