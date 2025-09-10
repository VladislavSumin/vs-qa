import ru.vladislavsumin.utils.fatJar

plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
    id("ru.vladislavsumin.convention.compose")
}

kotlin {
    val mainClassName = "ru.vladislavsumin.qa.MainKt"
    jvm {
        fatJar(
            mainClass = mainClassName,
            jarName = "vs-qa",
            duplicatesStrategy = DuplicatesStrategy.WARN,
        )
        mainRun {
            mainClass.set(mainClassName)
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.proguardParser)
            implementation(projects.core.ui.designSystem)
            implementation(projects.core.ui.hotkeyController)

            implementation(projects.feature.logViewer.impl)
            implementation(projects.feature.memoryIndicator.impl)

            implementation(vsCoreLibs.vs.core.logger.api)
            implementation(vsCoreLibs.vs.core.logger.platform)
            implementation(vsCoreLibs.vs.core.decompose.components)
            implementation(vsCoreLibs.vs.core.decompose.compose)
            implementation(vsCoreLibs.vs.core.di)

            implementation(vsCoreLibs.decompose.extensions.compose)
            implementation(vsCoreLibs.decompose.extensions.composeExperimental)

            implementation(compose.material3)
            implementation(compose.materialIconsExtended)

            implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
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
