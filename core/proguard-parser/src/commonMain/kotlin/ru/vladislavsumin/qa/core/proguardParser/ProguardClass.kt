package ru.vladislavsumin.qa.core.proguardParser

data class ProguardClass(
    val originalName: String,
    val obfuscatedName: String,
    val fields: List<ProguardField>,
    val methods: List<ProguardMethod>,
) {
    data class ProguardField(
        val originalName: String,
        val obfuscatedName: String,
        val type: String,
    )

    data class ProguardMethod(
        val originalName: String,
        val obfuscatedName: String,
        val returnType: String,
        // val arguments: String,
    )
}
