import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import proguard.gradle.ProGuardTask
import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.utils.fatJar
import java.io.File

plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
    id("ru.vladislavsumin.convention.kmp.android-application")
    id("ru.vladislavsumin.convention.compose")
    id("com.codingfeline.buildkonfig")
    id("io.sentry.kotlin.multiplatform.gradle")
}

android {
    namespace = "ru.vladislavsumin.qa"

    defaultConfig {
        applicationId = "ru.vladislavsumin.qa"
    }

    signingConfigs {
        named("debug") {
            storeFile = rootProject.rootDir.resolve("debug.jks")
            storePassword = "123456"
            keyAlias = "debug"
            keyPassword = "123456"
        }
    }

    buildTypes {
        release {
            // TODO нужна нормальная release подпись
            signingConfig = signingConfigs.getByName("debug")

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

kotlin {
    val mainClassName = "ru.vladislavsumin.qa.MainKt"
    jvm {
        fatJar(
            mainClass = mainClassName,
            jarName = "vs-qa",
            duplicatesStrategy = DuplicatesStrategy.WARN,
        ).configure {
            // TODO ждем пока jb починят
            // https://youtrack.jetbrains.com/issue/CMP-3859
            exclude("libskiko-macos-x64.*")
        }
        mainRun {
            mainClass.set(mainClassName)
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.ui.hotkeyController)

            implementation(projects.feature.bottomBar.impl)
            implementation(projects.feature.logViewer.impl)
            implementation(projects.feature.logParser.anime)
            implementation(projects.feature.memoryIndicator.impl)
            implementation(projects.feature.notifications.impl)
            implementation(projects.feature.rootScreen.impl)
            implementation(projects.feature.windowTitle.impl)

            implementation(vsCoreLibs.vs.core.logger.api)
            implementation(vsCoreLibs.vs.core.logger.platform)
            implementation(vsCoreLibs.vs.core.decompose.components)
            implementation(vsCoreLibs.vs.core.decompose.compose)
            implementation(vsCoreLibs.vs.core.di)
            implementation(vsCoreLibs.vs.core.navigation.impl)
            implementation(vsCoreLibs.vs.core.navigation.di)

            implementation(vsCoreLibs.decompose.extensions.compose)
            implementation(vsCoreLibs.decompose.extensions.composeExperimental)
        }

        jvmMain.dependencies {
            // Тянет набор библиотек для текущей платформы.
            // Тянет за собой кучу других compose библиотек, в том числе material.
            // Эта библиотека подключает специфичные апи заточенные под конкретную OS и конкретную архитектуру,
            // поэтому данный код не запустится на других платформах, кроме той на которой его собрали.
            // TODO сделать разделение по платформам.
            implementation(compose.desktop.currentOs)

            // Реализует Dispatchers.Main для Swing.
            implementation(vsCoreLibs.kotlin.coroutines.swing)
        }

        androidMain.dependencies {
            implementation(libs.android.activity.compose)
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

// Что бы выводились логи нужно запускать с флагом --info
// gradle app:buildFatJarMainMin --info
tasks.register<ProGuardTask>("buildFatJarMainMin") {
    dependsOn("buildFatJarMain")
    injars("build/libs/vs-qa.jar")
    outjars("build/libs/vs-qa-min.jar")

    // TODO разделить java/android конфигурации.
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
