package ru.vlasidlavsumin.core.stacktraceParser

data class StackTrace(
    val clazz: String,
    val message: String?,
    val elements: List<Element>,
) {
    data class Element(
        val clazz: String,
        val method: String,
        val file: String,
        val line: String,
    )

    override fun toString(): String {
        return buildString {
            append(clazz)
            if (message != null) {
                append(": ")
                append(message)
            }
            append("\n")
            elements.forEach { element ->
                append("\tat ")
                append(element.clazz)
                append(".")
                append(element.method)
                append("(")
                append(element.file)
                append(":")
                append(element.line)
                append(")")
                append("\n")
            }
        }
    }
}
