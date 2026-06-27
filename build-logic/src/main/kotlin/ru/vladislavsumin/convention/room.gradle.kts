package ru.vladislavsumin.convention

import ru.vladislavsumin.utils.libs

plugins {
    id("ru.vladislavsumin.convention.kmp.common")
    id("com.google.devtools.ksp")
    id("androidx.room")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }
    }

    targets.configureEach {
        if (name == "metadata") return@configureEach
        val kspConfigName = when (name) {
            "androidTarget" -> "kspAndroid"
            else -> "ksp" + name.replaceFirstChar { it.uppercaseChar() }
        }
        dependencies.add(kspConfigName, libs.room.compiler)
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}
