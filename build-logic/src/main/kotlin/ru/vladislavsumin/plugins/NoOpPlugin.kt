package ru.vladislavsumin.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Пустой плагин, нужен, что бы gradle подключил classpath в проект.
 * https://github.com/gradle/gradle/issues/18536
 */
class NoOpPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // no-op
    }
}
