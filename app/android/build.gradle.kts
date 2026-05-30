plugins {
    id("ru.vladislavsumin.convention.preset.app-android")
}

android {
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
            signingConfig = signingConfigs.getByName("debug")

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    lint {
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(projects.app.core)
    implementation(libs.android.activity.compose)

    implementation(projects.core.ui.hotkeyController)
    implementation(projects.feature.logViewer.impl)
    implementation(projects.feature.rootScreen.impl)

    implementation(vsCoreLibs.vs.core.decompose.compose)
    implementation(vsCoreLibs.vs.core.di)
    implementation(vsCoreLibs.vs.core.logger.api)
    implementation(vsCoreLibs.vs.core.navigation.impl)

    implementation(vsCoreLibs.decompose.core)
    implementation(vsCoreLibs.decompose.extensions.compose)
    implementation(vsCoreLibs.decompose.extensions.android)
    implementation(vsCoreLibs.kodein.core)
    implementation(vsCoreLibs.kodein.android)

    // TODO Включить обратно
    // implementation("io.sentry:sentry-android-core:8.14.0")
}
