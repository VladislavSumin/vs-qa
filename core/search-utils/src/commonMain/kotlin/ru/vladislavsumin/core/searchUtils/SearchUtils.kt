package ru.vladislavsumin.core.searchUtils

object SearchUtils {
    fun subsequenceSearch(text: String, pattern: String, ignoreCase: Boolean = false): List<IntRange>? {
        if (pattern.isEmpty()) return emptyList()
        if (text.isEmpty()) return null

        fun Char.normalize() = if (ignoreCase) lowercaseChar() else this

        val ranges = mutableListOf<IntRange>()
        var currentStartIndex = -1

        var j = 0
        var i = 0
        while (i < text.length) {
            if (text[i].normalize() == pattern[j].normalize()) {
                j++
                if (currentStartIndex == -1) {
                    currentStartIndex = i
                }
            } else {
                if (currentStartIndex >= 0) {
                    ranges += currentStartIndex..<i
                    currentStartIndex = -1
                }
            }
            if (j == pattern.length) break
            i++
        }
        if (currentStartIndex >= 0) {
            ranges += currentStartIndex..i
        }
        return if (j == pattern.length) ranges else null
    }
}
