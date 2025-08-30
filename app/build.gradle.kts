plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
    id("ru.vladislavsumin.convention.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(vsCoreLibs.vs.core.logger.api)
            implementation(vsCoreLibs.vs.core.logger.platform)
            implementation(vsCoreLibs.vs.core.decompose.components)
            implementation(vsCoreLibs.vs.core.decompose.compose)

            implementation(vsCoreLibs.decompose.extensions.compose)
            implementation(vsCoreLibs.decompose.extensions.composeExperimental)

            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
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