apply { from("build-logic/common-settings.gradle.kts") }

val useVsCoreSources = extra["ru.vs.core.useVsCoreSources"].toString().toBoolean()
if (useVsCoreSources) {
    includeBuild("../vs-core")
}

pluginManagement {
    includeBuild("build-logic")
}

rootProject.name = "vs-qa"

include(":app:core")
include(":app:android")
include(":app:jvm")

include(":core:adb:client")
include(":core:boyer-moore-search")
include(":core:proguard-parser")
include(":core:search-utils")
include(":core:ui:design-system")
include(":core:ui:drag-and-drop")
include(":core:ui:file-picker")
include(":core:ui:hotkey-controller")
include(":core:ui:hint")
include(":core:ui:selection")
include(":core:utils")

include(":feature:log-parser:api")
include(":feature:log-parser:anime")
include(":feature:log-parser:generic")
feature("adb-device")
feature("adb-device-list")
feature("bottom-bar")
feature("home-screen")
feature("log-recent")
feature("log-viewer")
feature("memory-indicator")
feature("multi-window")
feature("notifications")
feature("root-screen")
feature("tabs")
feature("window-title")

fun feature(name: String) {
    include(":feature:$name:api")
    include(":feature:$name:impl")
}
