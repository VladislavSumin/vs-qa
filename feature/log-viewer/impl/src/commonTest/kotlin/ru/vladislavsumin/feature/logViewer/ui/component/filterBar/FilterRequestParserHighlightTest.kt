package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import kotlinx.coroutines.flow.MutableStateFlow
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.manager.initTest
import ru.vladislavsumin.feature.logViewer.repository.SavedFiltersRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FilterRequestParserHighlightTest {
    init {
        LoggerManager.initTest()
    }

    @Test
    fun testEmpty() {
        assertEquals(
            expected = emptyList(),
            actual = createParser().categories(""),
        )
    }

    @Test
    fun testFieldContains() {
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Field to "tag",
                FilterRequestParser.Category.Operator to "=",
                FilterRequestParser.Category.Text to "search",
            ),
            actual = createParser().categories("tag=search"),
        )
    }

    @Test
    fun testFieldExactly() {
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Field to "tag",
                FilterRequestParser.Category.Operator to ":=",
                FilterRequestParser.Category.Text to "search",
            ),
            actual = createParser().categories("tag:=search"),
        )
    }

    @Test
    fun testQuotedAndPlainValueHaveSameCategory() {
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Field to "message",
                FilterRequestParser.Category.Operator to "=",
                FilterRequestParser.Category.Text to "2",
            ),
            actual = createParser().categories("message=2"),
        )
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Field to "message",
                FilterRequestParser.Category.Operator to "=",
                FilterRequestParser.Category.Text to "\"2\"",
            ),
            actual = createParser().categories("message=\"2\""),
        )
    }

    @Test
    fun testLogicOperators() {
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Text to "a",
                FilterRequestParser.Category.Logic to "&",
                FilterRequestParser.Category.Text to "b",
                FilterRequestParser.Category.Logic to "|",
                FilterRequestParser.Category.Text to "c",
            ),
            actual = createParser().categories("a & b | c"),
        )
    }

    @Test
    fun testNotAndMinusAreLogic() {
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Logic to "!",
                FilterRequestParser.Category.Text to "a",
                FilterRequestParser.Category.Logic to "-",
                FilterRequestParser.Category.Text to "b",
            ),
            actual = createParser().categories("!a -b"),
        )
    }

    @Test
    fun testMinusAfterOperatorIsText() {
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Field to "runNumber",
                FilterRequestParser.Category.Operator to "=",
                FilterRequestParser.Category.Text to "-",
                FilterRequestParser.Category.Text to "1",
            ),
            actual = createParser().categories("runNumber=-1"),
        )
    }

    @Test
    fun testRunNumberPositiveHighlight() {
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Field to "runNumber",
                FilterRequestParser.Category.Operator to "=",
                FilterRequestParser.Category.Text to "1",
            ),
            actual = createParser().categories("runNumber=1"),
        )
    }

    @Test
    fun testBrackets() {
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Bracket to "(",
                FilterRequestParser.Category.Text to "a",
                FilterRequestParser.Category.Bracket to ")",
            ),
            actual = createParser().categories("(a)"),
        )
    }

    @Test
    fun testAllFieldKeywords() {
        val fields = listOf(
            "tag", "pid", "tid", "thread", "message", "level", "runNumber", "timeAfter", "timeBefore",
        )
        fields.forEach { field ->
            assertEquals(
                expected = listOf(FilterRequestParser.Category.Field to field),
                actual = createParser().categories(field),
                message = "field $field must be highlighted as Field",
            )
        }
    }

    @Test
    fun testWhitespaceProducesNoSpans() {
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Text to "a",
                FilterRequestParser.Category.Text to "b",
            ),
            actual = createParser().categories("a  b"),
        )
    }

    @Test
    fun testSavedFilterRef() {
        val parser = createParser(listOf(SavedFiltersRepository.SavedFilter("myfilter", "tag=x")))
        assertEquals(
            expected = listOf(FilterRequestParser.Category.SavedFilterRef to "myfilter"),
            actual = parser.categories("myfilter"),
        )
    }

    @Test
    fun testSavedFilterRefAmongOthers() {
        val parser = createParser(listOf(SavedFiltersRepository.SavedFilter("myfilter", "tag=x")))
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.SavedFilterRef to "myfilter",
                FilterRequestParser.Category.Logic to "&",
                FilterRequestParser.Category.Field to "tag",
                FilterRequestParser.Category.Operator to "=",
                FilterRequestParser.Category.Text to "x",
            ),
            actual = parser.categories("myfilter & tag=x"),
        )
    }

    @Test
    fun testQuotedSavedFilterNameIsText() {
        val parser = createParser(listOf(SavedFiltersRepository.SavedFilter("myfilter", "tag=x")))
        assertEquals(
            expected = listOf(FilterRequestParser.Category.Text to "\"myfilter\""),
            actual = parser.categories("\"myfilter\""),
        )
    }

    @Test
    fun testUnknownWordIsText() {
        assertEquals(
            expected = listOf(FilterRequestParser.Category.Text to "myfilter"),
            actual = createParser().categories("myfilter"),
        )
    }

    @Test
    fun testUnbalancedQuoteIsInvalidSyntax() {
        val result = createParser().justHighlight("\"t\"\"")
        assertTrue(result is FilterRequestParser.RequestHighlight.InvalidSyntax)
    }

    @Test
    fun testQuoteInMiddleIsInvalidSyntax() {
        val result = createParser().justHighlight("a\"b")
        assertTrue(result is FilterRequestParser.RequestHighlight.InvalidSyntax)
    }

    @Test
    fun testValidQuotedStringIsSuccess() {
        assertEquals(
            expected = listOf(FilterRequestParser.Category.Text to "\"t\""),
            actual = createParser().categories("\"t\""),
        )
    }

    @Test
    fun testEscapedQuoteHighlightedAsSingleTextToken() {
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Text to "\"He said \\\"hello\\\" twice\"",
                FilterRequestParser.Category.Escape to "\\",
                FilterRequestParser.Category.Escape to "\\",
            ),
            actual = createParser().categories("\"He said \\\"hello\\\" twice\""),
        )
    }

    @Test
    fun testEmptyQuotedStringIsInvalidSyntax() {
        val result = createParser().justHighlight("\"\"")
        assertTrue(result is FilterRequestParser.RequestHighlight.InvalidSyntax)
    }

    @Test
    fun testUnclosedQuoteIsInvalidSyntax() {
        val result = createParser().justHighlight("\"a")
        assertTrue(result is FilterRequestParser.RequestHighlight.InvalidSyntax)
    }

    @Test
    fun testEscapeCharHighlightedAsEscapeCategory() {
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Text to "\"\\\"hello\\\"\"",
                FilterRequestParser.Category.Escape to "\\",
                FilterRequestParser.Category.Escape to "\\",
            ),
            actual = createParser().categories("\"\\\"hello\\\"\""),
        )
    }

    @Test
    fun testMultipleEscapesInString() {
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Text to "\"a\\\"b\\\"c\"",
                FilterRequestParser.Category.Escape to "\\",
                FilterRequestParser.Category.Escape to "\\",
            ),
            actual = createParser().categories("\"a\\\"b\\\"c\""),
        )
    }

    @Test
    fun testBackslashNotFollowedByQuoteIsNotEscape() {
        assertEquals(
            expected = listOf(FilterRequestParser.Category.Text to "\"a\\\\b\""),
            actual = createParser().categories("\"a\\\\b\""),
        )
    }

    @Test
    fun testSavedFilterNameAsFieldValueIsText() {
        val parser = createParser(listOf(SavedFiltersRepository.SavedFilter("myfilter", "tag=x")))
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Field to "tag",
                FilterRequestParser.Category.Operator to "=",
                FilterRequestParser.Category.Text to "myfilter",
            ),
            actual = parser.categories("tag=myfilter"),
        )
    }

    @Test
    fun testSavedFilterNameAsExactlyValueIsText() {
        val parser = createParser(listOf(SavedFiltersRepository.SavedFilter("myfilter", "tag=x")))
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Field to "message",
                FilterRequestParser.Category.Operator to ":=",
                FilterRequestParser.Category.Text to "myfilter",
            ),
            actual = parser.categories("message:=myfilter"),
        )
    }

    @Test
    fun testSavedFilterNameAsValueWithSpacesIsText() {
        val parser = createParser(listOf(SavedFiltersRepository.SavedFilter("myfilter", "tag=x")))
        assertEquals(
            expected = listOf(
                FilterRequestParser.Category.Field to "tag",
                FilterRequestParser.Category.Operator to "=",
                FilterRequestParser.Category.Text to "myfilter",
            ),
            actual = parser.categories("tag = myfilter"),
        )
    }

    private fun createParser(saved: List<SavedFiltersRepository.SavedFilter> = emptyList()) =
        FilterRequestParser(MutableStateFlow(saved))

    private fun FilterRequestParser.categories(request: String): List<Pair<FilterRequestParser.Category, String>> {
        val result = justHighlight(request) as FilterRequestParser.RequestHighlight.Success
        return result.spans.map { it.category to request.substring(it.range) }
    }
}
