import ru.vladislavsumin.utils.registerExternalModuleDetektTask

plugins {
    id("ru.vladislavsumin.convention.analyze.detekt-all")
    id("ru.vladislavsumin.plugins.noOpPlugin") apply false
}

registerExternalModuleDetektTask("detektBuildLogic", projectDir.resolve("build-logic"))
