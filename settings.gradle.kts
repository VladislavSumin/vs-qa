apply { from("build-logic/common-settings.gradle.kts") }

includeBuild("../vs-core")
pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "vs-qa"

include(":app")

include(":core:adb:client")
include(":core:proguard-parser")
include(":core:ui:design-system")
include(":core:ui:drag-and-drop")
include(":core:ui:file-picker")
include(":core:ui:hotkey-controller")
include(":core:utils")

include(":feature:log-parser:api")
include(":feature:log-parser:anime")
include(":feature:log-parser:generic")
feature("bottom-bar")
feature("home-screen")
feature("log-viewer")
feature("memory-indicator")
feature("notifications")
feature("root-screen")
feature("window-title")

fun feature(name: String) {
    include(":feature:$name:api")
    include(":feature:$name:impl")
}
