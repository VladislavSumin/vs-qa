package ru.vladislavsumin.core.boyerMooreSearch

/**
 * Поиск вхождений в строке по алгоритму Бойера - Мура.
 *
 * Вычисляет паттерн заранее облегчая дальнейший поиск.
 *
 * @param pattern подстрока для поиска.
 * @param ignoreCase игнорировать ли регистр при поиске.
 */
class BoyerMoorePattern internal constructor(
    pattern: String,
    private val ignoreCase: Boolean = false,
) {
    private val normalizedPattern: String = if (ignoreCase) pattern.lowercase() else pattern

    private val patternLength = normalizedPattern.length
    private val badChars: Map<Char, Int> = preprocessBadCharacter()

    private fun preprocessBadCharacter(): Map<Char, Int> {
        val map = mutableMapOf<Char, Int>()
        for (i in normalizedPattern.indices) {
            map[normalizedPattern[i]] = i
        }
        return map
    }

    private fun normalizeChar(c: Char): Char {
        return if (ignoreCase) c.lowercaseChar() else c
    }

    /**
     * Ищет все вхождения [normalizedPattern] в [text].
     * @return список индексов начал всех найденный вхождений.
     */
    fun search(text: String): List<Int> {
        val result = mutableListOf<Int>()
        val textLength = text.length

        if (patternLength == 0 || textLength < patternLength) return result

        var shift = 0

        while (shift <= textLength - patternLength) {
            var j = patternLength - 1

            while (j >= 0 && normalizedPattern[j] == normalizeChar(text[shift + j])) {
                j--
            }

            if (j < 0) {
                result.add(shift)

                shift += if (shift + patternLength < textLength) {
                    val nextChar = normalizeChar(text[shift + patternLength])
                    val lastIndex = badChars[nextChar] ?: -1
                    patternLength - lastIndex
                } else {
                    1
                }
            } else {
                val badCharIndex = badChars[normalizeChar(text[shift + j])] ?: -1
                val shiftAmount = j - badCharIndex
                shift += maxOf(1, shiftAmount)
            }
        }

        return result
    }
}

fun String.toBoyerMoorePattern(ignoreCase: Boolean = false): BoyerMoorePattern = BoyerMoorePattern(this, ignoreCase)
