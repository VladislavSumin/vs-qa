import ru.vladislavsumin.utils.registerExternalModuleDetektTask

plugins {
    id("ru.vladislavsumin.convention.analyze.detekt-all")
}

registerExternalModuleDetektTask("detektBuildLogic", projectDir.resolve("build-logic"))
