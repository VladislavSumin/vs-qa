package ru.vladislavsumin.convention.preset

import ru.vladislavsumin.utils.vsCoreLibs

plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")

    // Android тут выступает в качестве большого костыля. Дело в том что нам нужно минимум 2 таргета что бы
    // котлин плагин полностью настроил все части KMP. Иначе у нас не создается таска kspCommonMainKotlinMetadata
    // и не получается нормально подвязать KSP к проекту. Разобраться как сделать более приемлемый обход с ходу
    // да и не сходу тоже не получилось, поэтому пока живем так. Мб google когда-нибудь таки научат свой плагин
    // нормально работать.
    id("ru.vladislavsumin.convention.kmp.android-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(vsCoreLibs.vs.core.coroutines.utils)
            implementation(vsCoreLibs.vs.core.logger.api)
        }
    }
}
