package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import kotlinx.coroutines.flow.MutableStateFlow
import ru.vladislavsumin.core.logger.manager.LoggerManager
import ru.vladislavsumin.core.logger.manager.initTest
import ru.vladislavsumin.feature.logViewer.ui.component.filterHint.CurrentTokenPrediction
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("MaximumLineLength", "MaxLineLength")
class FilterRequestParserPredictionTest {
    init {
        LoggerManager.initTest()
    }

    @Test
    fun testField() {
        val parser = createParser()
        val prediction = parser.parse("m", cursorPosition = 1).currentTokenPredictionInfo!!
        assertEquals(
            expected = "m",
            actual = prediction.startText,
        )
        assertEquals(
            expected = CurrentTokenPrediction.Type.Keyword,
            actual = prediction.type,
        )
    }

    @Test
    fun testFilterType() {
        val parser = createParser()
        val prediction = parser.parse("message", cursorPosition = 7).currentTokenPredictionInfo!!
        assertEquals(
            expected = "",
            actual = prediction.startText,
        )
        assertEquals(
            expected = CurrentTokenPrediction.Type.SearchType,
            actual = prediction.type,
        )
    }

    @Test
    fun testFilterTypeWithPart() {
        val parser = createParser()
        val prediction = parser.parse("message:", cursorPosition = 8).currentTokenPredictionInfo!!
        assertEquals(
            expected = ":",
            actual = prediction.startText,
        )
        assertEquals(
            expected = CurrentTokenPrediction.Type.SearchType,
            actual = prediction.type,
        )
    }

    @Test
    fun testFieldWithOffset() {
        val parser = createParser()
        val prediction = parser.parse("message:", cursorPosition = 3).currentTokenPredictionInfo!!
        assertEquals(
            expected = "mes",
            actual = prediction.startText,
        )
        assertEquals(
            expected = CurrentTokenPrediction.Type.Keyword,
            actual = prediction.type,
        )
    }

    @Test
    fun testRunNumberPositivePrediction() {
        val parser = createParser()
        val prediction = parser.parse("runNumber=1", cursorPosition = 11).currentTokenPredictionInfo!!
        assertEquals(
            expected = "1",
            actual = prediction.startText,
        )
        assertEquals(
            expected = CurrentTokenPrediction.Type.RunNumber,
            actual = prediction.type,
        )
    }

    @Test
    fun testRunNumberMinusPrediction() {
        val parser = createParser()
        val prediction = parser.parse("runNumber=-", cursorPosition = 11).currentTokenPredictionInfo!!
        assertEquals(
            expected = "-",
            actual = prediction.startText,
        )
        assertEquals(
            expected = CurrentTokenPrediction.Type.RunNumber,
            actual = prediction.type,
        )
    }

    @Test
    fun testRunNumberPositiveMultiDigit() {
        val parser = createParser()
        val prediction = parser.parse("runNumber=12", cursorPosition = 12).currentTokenPredictionInfo!!
        assertEquals(
            expected = "12",
            actual = prediction.startText,
        )
        assertEquals(
            expected = CurrentTokenPrediction.Type.RunNumber,
            actual = prediction.type,
        )
    }

    @Test
    fun testRunNumberNegativePrediction() {
        val parser = createParser()
        val prediction = parser.parse("runNumber=-1", cursorPosition = 12).currentTokenPredictionInfo!!
        assertEquals(
            expected = "-1",
            actual = prediction.startText,
        )
        assertEquals(
            expected = CurrentTokenPrediction.Type.RunNumber,
            actual = prediction.type,
        )
    }

    @Test
    fun testRunNumberNegativeMultiDigit() {
        val parser = createParser()
        val prediction = parser.parse("runNumber=-12", cursorPosition = 13).currentTokenPredictionInfo!!
        assertEquals(
            expected = "-12",
            actual = prediction.startText,
        )
        assertEquals(
            expected = CurrentTokenPrediction.Type.RunNumber,
            actual = prediction.type,
        )
    }

    private fun createParser() = FilterRequestParser(savedFilters = MutableStateFlow(emptyList()))
}
