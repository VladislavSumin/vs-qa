package ru.vladislavsumin.core.boyerMooreSearch

import kotlin.test.Test
import kotlin.test.assertEquals

class BoyerMoorePatternTest {

    @Test
    fun testSingleMatch() {
        val bm = BoyerMoorePattern("ABC")
        assertEquals(listOf(4), bm.search("ABAAABCD"))
    }

    @Test
    fun testMultipleMatches() {
        val bm = BoyerMoorePattern("ABC")
        assertEquals(listOf(3, 6), bm.search("XYZABCABC"))
    }

    @Test
    fun testNoMatches() {
        val bm = BoyerMoorePattern("ABC")
        assertEquals(emptyList(), bm.search("ZZZZZZ"))
    }

    @Test
    fun testPatternEqualsText() {
        val bm = BoyerMoorePattern("HELLO")
        assertEquals(listOf(0), bm.search("HELLO"))
    }

    @Test
    fun testPatternLongerThanText() {
        val bm = BoyerMoorePattern("LONGPATTERN")
        assertEquals(emptyList(), bm.search("SHORT"))
    }

    @Test
    fun testEmptyPattern() {
        val bm = BoyerMoorePattern("")
        assertEquals(emptyList(), bm.search("ANY TEXT"))
    }

    @Test
    fun testUnicodeCyrillic() {
        val bm = BoyerMoorePattern("мир")
        assertEquals(listOf(7, 21), bm.search("Привет мир, огромный мир"))
    }

    @Test
    fun testUnicodeEmoji() {
        val bm = BoyerMoorePattern("🌍")
        assertEquals(listOf(0, 2, 4), bm.search("🌍🌍🌍"))
    }

    @Test
    fun testOverlappingMatches() {
        val bm = BoyerMoorePattern("AAA")
        assertEquals(listOf(0, 1, 2), bm.search("AAAAA"))
    }

    @Test
    fun testSpecialCharacters() {
        val bm = BoyerMoorePattern($$"a$b")
        assertEquals(listOf(2, 5), bm.search($$"xxa$ba$b"))
    }

    // =========================
    // 🔥 НОВЫЕ ТЕСТЫ ignoreCase
    // =========================

    @Test
    fun testIgnoreCaseBasic() {
        val bm = BoyerMoorePattern("abc", ignoreCase = true)
        assertEquals(listOf(0, 4, 8), bm.search("AbC abc ABC"))
    }

    @Test
    fun testIgnoreCaseNoMatchWhenDisabled() {
        val bm = BoyerMoorePattern("abc", ignoreCase = false)
        assertEquals(listOf(4), bm.search("AbC abc ABC"))
    }

    @Test
    fun testIgnoreCaseCyrillic() {
        val bm = BoyerMoorePattern("МИР", ignoreCase = true)
        assertEquals(listOf(7, 21), bm.search("Привет мир, огромный Мир"))
    }

    @Test
    fun testIgnoreCaseMixedPositions() {
        val bm = BoyerMoorePattern("TeSt", ignoreCase = true)
        assertEquals(listOf(0, 5, 10), bm.search("test TEST TeSt"))
    }

    @Test
    fun testIgnoreCaseSingleChar() {
        val bm = BoyerMoorePattern("a", ignoreCase = true)
        assertEquals(listOf(0, 1, 2, 3), bm.search("AaAa"))
    }

    @Test
    fun testIgnoreCaseWithSpecialChars() {
        val bm = BoyerMoorePattern($$"A$B", ignoreCase = true)
        assertEquals(listOf(0, 3), bm.search($$"a$bA$B"))
    }

    @Test
    fun testIgnoreCaseEmojiUnchanged() {
        val bm = BoyerMoorePattern("🌍", ignoreCase = true)
        assertEquals(listOf(0, 2, 4), bm.search("🌍🌍🌍"))
    }

    @Test
    fun testIgnoreCasePartialOverlap() {
        val bm = BoyerMoorePattern("AaA", ignoreCase = true)
        assertEquals(listOf(0, 1, 2), bm.search("aaaaa"))
    }
}
