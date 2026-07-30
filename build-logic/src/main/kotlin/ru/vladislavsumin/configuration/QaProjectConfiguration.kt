package ru.vladislavsumin.configuration

import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType

@Suppress("UnnecessaryAbstractClass")
open class QaProjectConfiguration(project: Project, propertyProvider: PropertyProvider) :
    Configuration(project, "ru.vs.qa", propertyProvider) {

    val composeStabilityAnalyzer = ComposeStabilityAnalyzer()

    inner class ComposeStabilityAnalyzer : Configuration("composeStabilityAnalyzer", this) {
        val enabled = property("enabled", false)
        val traceAll = property("traceAll", false)
    }
}

val Project.qaProjectConfiguration: QaProjectConfiguration
    get() = rootProject.extensions.findByType()
        ?: rootProject.extensions.create(
            QaProjectConfiguration::class.java.simpleName,
            project,
            propertyProvider,
        )

private val Project.propertyProvider
    get() = PropertyProvider { System.getenv(it) ?: project.findProperty(it)?.toString() }
