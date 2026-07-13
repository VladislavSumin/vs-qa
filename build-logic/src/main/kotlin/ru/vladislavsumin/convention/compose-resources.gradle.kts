package ru.vladislavsumin.convention

import org.jetbrains.compose.resources.ResourcesExtension
import ru.vladislavsumin.configuration.projectConfiguration
import ru.vladislavsumin.utils.fullNameAsNamespace
import ru.vladislavsumin.utils.protectFromDslAccessors

/**
 * Автоматически настраивает compose ресурсы для модуля.
 *
 * Пакет генерируемого класса [Res] выводится из полного имени модуля по аналогии с android namespace:
 * `${basePackage}.${fullNameAsNamespace}.generated.resources`.
 *
 * Благодаря этому строковые ресурсы можно добавлять в любой модуль просто положив файлы в
 * `composeResources`, без ручной настройки в build.gradle.kts.
 */

plugins {
    id("ru.vladislavsumin.convention.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.components.resources)
        }
    }
}

protectFromDslAccessors {
    compose {
        resources {
            generateResClass = ResourcesExtension.ResourceClassGeneration.Always
            packageOfResClass = "${projectConfiguration.basePackage}.${fullNameAsNamespace()}.generated.resources"
        }
    }
}
