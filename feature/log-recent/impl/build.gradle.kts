plugins {
    id("ru.vladislavsumin.convention.preset.feature-impl-ui")
    id("androidx.room")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }
    }
}

// TODO сделать соглашение для room
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
}
