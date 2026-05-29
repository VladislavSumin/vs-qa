package ru.vladislavsumin.core.searchUtils

import kotlin.test.Test
import kotlin.test.assertEquals

class SubsequenceSearchTest {
    @Test
    fun testEqualsTextAndPattern() {
        val result = SearchUtils.subsequenceSearch("AAA", "AAA")
        assertEquals(listOf(0..<3), result)
        assertEquals("AAA", "AAAAAAAA".substring(0..<3))
    }

    @Test
    fun testStartWithPattern() {
        val result = SearchUtils.subsequenceSearch("AAABBB", "AAA")
        assertEquals(listOf(0..<3), result)
    }

    @Test
    fun testEndWithPattern() {
        val result = SearchUtils.subsequenceSearch("AAABBB", "BBB")
        assertEquals(listOf(3..<6), result)
    }

    @Test
    fun testMiddleWithPattern() {
        val result = SearchUtils.subsequenceSearch("AAABBBAAA", "BBB")
        assertEquals(listOf(3..<6), result)
    }

    @Test
    fun testSubsequence() {
        val result = SearchUtils.subsequenceSearch("ABABABA", "BB")
        assertEquals(listOf(1..<2, 3..<4), result)
    }

    @Test
    fun testEmptyPattern() {
        val result = SearchUtils.subsequenceSearch("AAA", "")
        assertEquals(emptyList(), result)
    }

    @Test
    fun testEmptyText() {
        val result = SearchUtils.subsequenceSearch("", "AAA")
        assertEquals(null, result)
    }

    @Test
    fun testEmptyTextAndEmptyPattern() {
        val result = SearchUtils.subsequenceSearch("", "")
        assertEquals(emptyList(), result)
    }
}
