package ru.vladislavsumin.convention

plugins {
    id("com.mikepenz.aboutlibraries.plugin")
}

aboutLibraries {
    export {
        outputFile = rootProject.file(
            "feature/legal-info/impl/src/commonMain/composeResources/files/aboutlibraries.json",
        )
        prettyPrint = true
    }
}
