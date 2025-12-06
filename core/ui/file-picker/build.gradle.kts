plugins {
    id("ru.vladislavsumin.convention.preset.core")
    id("ru.vladislavsumin.convention.compose")
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            // TODO вынести в конвеншен?
            implementation(libs.android.activity.compose)
        }
    }
}
