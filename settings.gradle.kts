apply { from("../vs-core/build-scripts/common-settings.gradle.kts") }

includeBuild("../vs-core")
pluginManagement {
    includeBuild("../vs-core/build-scripts")
}

rootProject.name = "vs-qa"

include(":app")

include(":core:proguard-parser")
include(":core:ui:design-system")
include(":core:ui:hotkey-controller")
include(":core:utils")

feature("log-viewer")
feature("memory-indicator")

fun feature(name: String) {
    include(":feature:$name:api")
    include(":feature:$name:impl")
}
