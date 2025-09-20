package ru.vladislavsumin.convention

/**
 * Хак для подключения KSP к commonMain в kmp модулях с java only таргетом.
 * После подключения добавляем ksp вот так:
 * ```
 * dependencies {
 *     add("kspJvm", vsCoreLibs.vs.core.factoryGenerator.ksp)
 * }
 * ```
 * p.s. Нет, мне не стыдно за этот код, стыдно должно быть гуглу за то что допустили все это, за то что игнорируют
 * issues по 5 и более лет. Вот за это должно быть стыдно.
 */

plugins {
    id("ru.vladislavsumin.convention.kmp.jvm")
    id("com.google.devtools.ksp")
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}

val kspSourcesCopyTask = tasks.register<Copy>("copyKspSourcesFromJvmToCommonSourceSet") {
    dependsOn("kspKotlinJvm")
    from("build/generated/ksp/jvm/jvmMain/kotlin")
    into("build/generated/ksp/metadata/commonMain/kotlin")
}
tasks.named("compileKotlinJvm") { dependsOn(kspSourcesCopyTask) }

// Исключаем сгенерированные jvm ksp исходные коды из компиляции.
// К сожалению студия не настолько умна, что бы понять этот хитрый ход, поэтому все еще будет подсвечивать эту папку как
// исходные коды jvmMain и даже если мы явно добавим ее как исходные коды commonMain то, хоть проект и будет собираться,
// но подсветка синтаксиса работать не будет. Поэтому используем прикол с copy выше.
kotlin.sourceSets.jvmMain.configure {
    this.kotlin {
        this.exclude { element ->
            // Нормальную проверку тут сделать нельзя из-за кешей поэтому так
            val file = element.file.relativeToOrNull(File(".").absoluteFile)
            file?.path?.contains("build/generated/ksp/jvm/jvmMain/kotlin") == true
        }
    }
}
