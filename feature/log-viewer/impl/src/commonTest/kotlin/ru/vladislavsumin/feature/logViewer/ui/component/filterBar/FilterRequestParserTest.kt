package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import kotlinx.coroutines.flow.MutableStateFlow
import ru.vladislavsumin.core.logger.common.LogLevel
import ru.vladislavsumin.core.logger.manager.ExternalLogger
import ru.vladislavsumin.core.logger.manager.LoggerManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("MaximumLineLength", "MaxLineLength")
class FilterRequestParserTest {
    init {
        TestLogger.init()
    }

    @Test
    fun testEmpty() {
        val parser = createParser()
        val request = parser.tokenize("").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "NoOp",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testSimple() {
        val parser = createParser()
        val request = parser.tokenize("search").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "All(operation=Contains(data=search))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testSimpleQuoted() {
        val parser = createParser()
        val request = parser.tokenize("\"search\"").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "All(operation=Contains(data=search))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testFieldContains() {
        val parser = createParser()
        val request = parser.tokenize("tag=search").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Tag(operation=Contains(data=search))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testFieldExactly() {
        val parser = createParser()
        val request = parser.tokenize("tag:=search").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Tag(operation=Exactly(data=search))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testNot() {
        val parser = createParser()
        val request = parser.tokenize("!search").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Not(operation=All(operation=Contains(data=search)))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testFieldNot() {
        val parser = createParser()
        val request = parser.tokenize("!tag=search").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Not(operation=Tag(operation=Contains(data=search)))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testOr() {
        val parser = createParser()
        val request = parser.tokenize("a | b").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Or(operations=[All(operation=Contains(data=a)), All(operation=Contains(data=b))])",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testAutoOr() {
        val parser = createParser()
        val request = parser.tokenize("tag=a tag=b").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Auto(operation=Or(operations=[Tag(operation=Contains(data=a)), Tag(operation=Contains(data=b))]))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testAutoAnd() {
        val parser = createParser()
        val request = parser.tokenize("tag=a b").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Auto(operation=And(operations=[Tag(operation=Contains(data=a)), All(operation=Contains(data=b))]))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testAutoAndOr() {
        val parser = createParser()
        val request = parser.tokenize("tag=a tag=b c").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Auto(operation=And(operations=[Or(operations=[Tag(operation=Contains(data=a)), Tag(operation=Contains(data=b))]), All(operation=Contains(data=c))]))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testAutoAndOrSmellOrder() {
        val parser = createParser()
        val request = parser.tokenize("tag=a c tag=b").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Auto(operation=And(operations=[Or(operations=[Tag(operation=Contains(data=a)), Tag(operation=Contains(data=b))]), All(operation=Contains(data=c))]))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testAutoPriorityAnd() {
        val parser = createParser()
        val request = parser.tokenize("tag=a tag=b & c").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Auto(operation=And(operations=[Tag(operation=Contains(data=a)), And(operations=[Tag(operation=Contains(data=b)), All(operation=Contains(data=c))])]))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testAutoPriorityOr() {
        val parser = createParser()
        val request = parser.tokenize("tag=a tag=b | c").searchRequest
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
        val request = parser.tokenize("a|b").searchRequest
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
        val request = parser.tokenize("()").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "NoOp",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testSimpleBracket() {
        val parser = createParser()
        val request = parser.tokenize("(simple)").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "All(operation=Contains(data=simple))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testSimpleDoubleBracket() {
        val parser = createParser()
        val request = parser.tokenize("((simple))").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "All(operation=Contains(data=simple))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testOrNot() {
        val parser = createParser()
        val request = parser.tokenize("a | !b").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Or(operations=[All(operation=Contains(data=a)), Not(operation=All(operation=Contains(data=b)))])",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    @Test
    fun testNotSimpleBracket() {
        val parser = createParser()
        val request = parser.tokenize("!(simple)").searchRequest
        assertTrue(request.isSuccess)
        assertEquals(
            expected = "Not(operation=All(operation=Contains(data=simple)))",
            actual = request.getOrThrow().operation.toString(),
        )
    }

    private fun createParser() = FilterRequestParser(savedFilters = MutableStateFlow(emptyList()))
}

// TODO вынести в базовые методы.
private object TestLogger {
    private var isInitialized = false
    fun init() {
        if (!isInitialized) {
            isInitialized = true
            LoggerManager.init(externalLoggerFactory = {
                object : ExternalLogger {
                    override fun log(level: LogLevel, msg: String) {
                        // no_op
                    }

                    override fun log(
                        level: LogLevel,
                        throwable: Throwable,
                        msg: String,
                    ) {
                        // no_op
                    }
                }
            })
        }
    }
}
