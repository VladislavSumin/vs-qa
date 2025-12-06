plugins {
    `kotlin-dsl`
}

dependencies {
    // Мы хотим получать доступ к libs из наших convention плагинов, но гредл на текущий момент не умеет прокидывать
    // version catalogs. Поэтому используем костыль отсюда - https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation("ru.vladislavsumin:build-scripts")

    implementation(vsCoreLibs.gradlePlugins.kotlin.core)
    implementation(vsCoreLibs.gradlePlugins.jb.compose)
    implementation(libs.gradlePlugins.sentry)
    implementation(libs.gradlePlugins.buildkonfig)
    implementation(libs.gradlePlugins.proguard)
}

gradlePlugin {
    plugins {
        create("noOpPlugin") {
            id = "ru.vladislavsumin.plugins.noOpPlugin"
            implementationClass = "ru.vladislavsumin.plugins.NoOpPlugin"
        }
    }
}
