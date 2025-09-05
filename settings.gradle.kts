apply { from("../vs-core/build-scripts/common-settings.gradle.kts") }

includeBuild("../vs-core")
pluginManagement {
    includeBuild("../vs-core/build-scripts")
}

rootProject.name = "vs-qa"

include(":app")

include(":core:proguard-parser")
include(":core:stacktrace-parser")
