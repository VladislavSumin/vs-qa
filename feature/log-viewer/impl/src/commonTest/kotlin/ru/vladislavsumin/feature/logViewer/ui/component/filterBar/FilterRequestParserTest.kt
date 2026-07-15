package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import kotlinx.coroutines.flow.MutableStateFlow
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.manager.initTest
import ru.vladislavsumin.feature.logViewer.repository.SavedFiltersRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("MaximumLineLength", "MaxLineLength")
class FilterRequestParserTest {
    init {
        LoggerManager.initTest()
    }

    @Test
    fun testEmpty() {
        val parser = createParser()
        val request = parser.parse("").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "NoOp",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testSimple() {
        val parser = createParser()
        val request = parser.parse("search").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "All(operation=Contains(data=search))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testSimpleQuoted() {
        val parser = createParser()
        val request = parser.parse("\"search\"").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "All(operation=Contains(data=search))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testFieldContains() {
        val parser = createParser()
        val request = parser.parse("tag=search").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Tag(operation=Contains(data=search))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testFieldExactly() {
        val parser = createParser()
        val request = parser.parse("tag:=search").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Tag(operation=Exactly(data=search))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testNot() {
        val parser = createParser()
        val request = parser.parse("!search").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Not(operation=All(operation=Contains(data=search)))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testFieldNot() {
        val parser = createParser()
        val request = parser.parse("!tag=search").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Not(operation=Tag(operation=Contains(data=search)))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testOr() {
        val parser = createParser()
        val request = parser.parse("a | b").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Or(operations=[All(operation=Contains(data=a)), All(operation=Contains(data=b))])",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testAutoOr() {
        val parser = createParser()
        val request = parser.parse("tag=a tag=b").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Auto(operation=Or(operations=[Tag(operation=Contains(data=a)), Tag(operation=Contains(data=b))]))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testAutoAnd() {
        val parser = createParser()
        val request = parser.parse("tag=a b").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Auto(operation=And(operations=[Tag(operation=Contains(data=a)), All(operation=Contains(data=b))]))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testAutoAndOr() {
        val parser = createParser()
        val request = parser.parse("tag=a tag=b c").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Auto(operation=And(operations=[Or(operations=[Tag(operation=Contains(data=a)), Tag(operation=Contains(data=b))]), All(operation=Contains(data=c))]))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testAutoAndOrSmellOrder() {
        val parser = createParser()
        val request = parser.parse("tag=a c tag=b").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Auto(operation=And(operations=[Or(operations=[Tag(operation=Contains(data=a)), Tag(operation=Contains(data=b))]), All(operation=Contains(data=c))]))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testAutoPriorityAnd() {
        val parser = createParser()
        val request = parser.parse("tag=a tag=b & c").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Auto(operation=And(operations=[Tag(operation=Contains(data=a)), And(operations=[Tag(operation=Contains(data=b)), All(operation=Contains(data=c))])]))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testAutoPriorityOr() {
        val parser = createParser()
        val request = parser.parse("tag=a tag=b | c").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Auto(operation=And(operations=[Tag(operation=Contains(data=a)), Or(operations=[Tag(operation=Contains(data=b)), All(operation=Contains(data=c))])]))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

// TODO я даже хз нужно ли такую срань закреплять?
//    @Test
//    fun testAutoPriorityDoubleAnd() {
//        val parser = createParser()
//        val request = parser.tokenize("a & b c & d").searchRequest
//        assertTrue(request.isSuccess)
//        assertEquals(
//            expected = "Auto(operation=Or(operations=[And(operations=[All(operation=Contains(data=a)), All(operation=Contains(data=b))]), And(operations=[All(operation=Contains(data=c)), All(operation=Contains(data=d))])]))",
//            actual = request.getOrThrow().operation.toString(),
//        )
//    }

    @Test
    fun testOrNoSpaces() {
        val parser = createParser()
        val request = parser.parse("a|b").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            // Это не ошибка, кажется такое вполне логично.
            expected = "All(operation=Contains(data=a|b))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testEmptyBracket() {
        val parser = createParser()
        val request = parser.parse("()").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "NoOp",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testSimpleBracket() {
        val parser = createParser()
        val request = parser.parse("(simple)").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "All(operation=Contains(data=simple))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testSimpleDoubleBracket() {
        val parser = createParser()
        val request = parser.parse("((simple))").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "All(operation=Contains(data=simple))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testOrNot() {
        val parser = createParser()
        val request = parser.parse("a | !b").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Or(operations=[All(operation=Contains(data=a)), Not(operation=All(operation=Contains(data=b)))])",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testNotSimpleBracket() {
        val parser = createParser()
        val request = parser.parse("!(simple)").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Not(operation=All(operation=Contains(data=simple)))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testSavedFilterExpansion() {
        val parser = createParser(listOf(SavedFiltersRepository.SavedFilter("err", "tag=Error")))
        val request = parser.parse("err").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Tag(operation=Contains(data=Error))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testSavedFilterExpansionInExpression() {
        val parser = createParser(listOf(SavedFiltersRepository.SavedFilter("err", "tag=Error")))
        val request = parser.parse("err & b").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "And(operations=[Tag(operation=Contains(data=Error)), All(operation=Contains(data=b))])",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testLevel() {
        val parser = createParser()
        val request = parser.parse("level=e").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "MinLogLevel(minLevel=ERROR)",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testRunNumber() {
        val parser = createParser()
        val request = parser.parse("runNumber=1").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "RunNumber(number=1)",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testRunNumberNegative() {
        val parser = createParser()
        val request = parser.parse("runNumber=-1").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "RunNumber(number=-1)",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testTimeAfter() {
        val parser = createParser()
        val request = parser.parse("timeAfter=12:00:00").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "TimeAfter(time=12:00:00)",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testTimeBefore() {
        val parser = createParser()
        val request = parser.parse("timeBefore=12:00:00").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "TimeBefore(time=12:00:00)",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testUnbalancedQuoteParseFailure() {
        val parser = createParser()
        val request = parser.parse("\"t\"\"").searchRequest
        assertTrue(request.isFailure)
    }

    @Test
    fun testQuoteInMiddleParseFailure() {
        val parser = createParser()
        val request = parser.parse("a\"b").searchRequest
        assertTrue(request.isFailure)
    }

    @Test
    fun testValidQuotedStringStillParses() {
        val parser = createParser()
        val request = parser.parse("\"t\"").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "All(operation=Contains(data=t))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testEscapedQuoteInString() {
        val parser = createParser()
        val request = parser.parse("\"He said \\\"hello\\\" twice\"").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "All(operation=Contains(data=He said \"hello\" twice))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testEscapedQuoteOnly() {
        val parser = createParser()
        val request = parser.parse("\"\\\"\"").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "All(operation=Contains(data=\"))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    private fun createParser(saved: List<SavedFiltersRepository.SavedFilter> = emptyList()) =
        FilterRequestParser(savedFilters = MutableStateFlow(saved))
}
