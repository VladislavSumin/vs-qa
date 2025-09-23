import proguard.gradle.ProGuardTask
import ru.vladislavsumin.utils.fatJar
import java.io.File

plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
    id("ru.vladislavsumin.convention.compose")
}

buildscript {
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.7.0")
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
            implementation(projects.feature.bottomBar.impl)
            implementation(projects.feature.logViewer.impl)
            implementation(projects.feature.logParser.anime)
            implementation(projects.feature.memoryIndicator.impl)
            implementation(projects.feature.rootScreen.impl)
            implementation(projects.feature.windowTitle.impl)

            implementation(vsCoreLibs.vs.core.logger.api)
            implementation(vsCoreLibs.vs.core.logger.platform)
            implementation(vsCoreLibs.vs.core.decompose.components)
            implementation(vsCoreLibs.vs.core.decompose.compose)
            implementation(vsCoreLibs.vs.core.di)
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
