package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import com.github.h0tk3y.betterParse.combinators.OrCombinator
import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.asJust
import com.github.h0tk3y.betterParse.combinators.leftAssociative
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.LiteralToken
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.TokenMatchesSequence
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.noneMatched
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import kotlinx.coroutines.flow.StateFlow
import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logViewer.TokenPredictionLogger
import ru.vladislavsumin.feature.logViewer.domain.logs.FilterRequest
import ru.vladislavsumin.feature.logViewer.repository.SavedFiltersRepository
import ru.vladislavsumin.feature.logViewer.ui.component.filterHint.CurrentTokenPrediction
import kotlin.map
import kotlin.time.measureTimedValue

internal class FilterRequestParser(private val savedFilters: StateFlow<List<SavedFiltersRepository.SavedFilter>>) {

    data class ParserResult(
        val requestHighlight: RequestHighlight,
        val searchRequest: Result<FilterRequest>,
        val currentTokenPredictionInfo: CurrentTokenPrediction?,
    )

    sealed interface RequestHighlight {
        val raw: String

        data class Success(override val raw: String, val spans: List<Span>) : RequestHighlight

        data class InvalidSyntax(override val raw: String) : RequestHighlight
    }

    data class Span(val range: IntRange, val category: Category)

    enum class Category {
        Field,
        Operator,
        Logic,
        Bracket,
        Escape,
        SavedFilterRef,
        Text,
    }

    private enum class Operation {
        Exactly,
        Contains,
    }

    private enum class Field {
        All,

        Tag,
        ProcessId,
        Thread,
        Message,
    }

    @Suppress("UnusedPrivateProperty")
    private val grammar = object : Grammar<FilterRequest.FilterOperation>() {
        val tag by literalToken("tag")
        private val pid by literalToken("pid")
        private val tid by literalToken("tid")
        private val thread by literalToken("thread")
        private val message by literalToken("message")
        val level by literalToken("level")

        val runNumber by literalToken("runNumber")
        private val timeAfter by literalToken("timeAfter")
        private val timeBefore by literalToken("timeBefore")

        private val exactly by literalToken(":=")
        private val contains by literalToken("=")

        private val not by literalToken("!")
        val minus by literalToken("-")

        private val and by literalToken("&")
        private val or by literalToken("|")

        private val lpar by literalToken("(")
        private val rpar by literalToken(")")

        // Строка в кавычках, может содержать экранированные кавычки внутри
        private val stringLiteral by regexToken("\"(\\\\\"|[^\"])+\"")

        // Любая строка без пробелов, скобок и кавычек.
        private val any by regexToken("[^ \n()\"]+")

        // Определяются последними чтобы не перебивать собой другие токены, например stringLiteral.
        private val ws by regexToken("\\s+", ignore = true)
        private val newLine by literalToken("\n", ignore = true)

        val tokenGroupFields = setOf(
            tag, pid, tid, thread, message, level, runNumber, timeAfter, timeBefore,
        )
        val tokenGroupFilterType = setOf(
            exactly,
            contains,
        )
        val tokenGroupLogic = setOf(
            not,
            minus,
            and,
            or,
        )
        val tokenGroupBrackets = setOf(
            lpar,
            rpar,
        )

        val tokenGroupStringLiteral = setOf(stringLiteral)
        val tokenGroupText = setOf(any)

        // Поля по которым можно вести поиск.
        val fields = OrCombinator(
            listOf(
                tag asJust Field.Tag,
                pid asJust Field.ProcessId,
                tid asJust Field.Thread,
                thread asJust Field.Thread,
                message asJust Field.Message,
            ),
        )

        // Операции поиска
        val operations = OrCombinator(
            listOf(
                exactly asJust Operation.Exactly,
                contains asJust Operation.Contains,
            ),
        )

        // При поиске по [any] строке это может быть как обычная строка так название сохраненного фильтра.
        val unquotedMaybeSavedFilter = any map { token ->
            val content = token.text
            val savedFilter = savedFilters.value.find { it.name == content }
            if (savedFilter != null) {
                val tokens = tokenizer.tokenize(savedFilter.content)
                parseToEnd(tokens)
            } else {
                FilterRequest.FilterOperation.All(FilterRequest.Operation.Contains(content))
            }
        }

        // Фильтры
        val filters = OrCombinator(
            listOf(
                stringLiteral map {
                    it.text
                        .substring(1, it.length - 1) // отрезаем внешние кавычки
                        .replace("\\\"", "\"") // убираем экраны с внутренних кавычек
                },
                any map { it.text },
            ),
        )

        private val levelFilter = (-level and -contains and filters) map { level ->
            val level = LogLevel.fromAlias(level) ?: error("Unknown level $level")
            FilterRequest.FilterOperation.MinLogLevel(level)
        }
        private val runNumberFilter = (-runNumber and -contains and optional(minus) and filters)
            .map { (maybeMinus, value) ->
                val sign = if (maybeMinus != null) -1 else 1
                FilterRequest.FilterOperation.RunNumber(sign * value.toInt())
            }
        private val timeBeforeFilter = (-timeBefore and -contains and filters) map {
            FilterRequest.FilterOperation.TimeBefore(it)
        }
        private val timeAfterFilter = (-timeAfter and -contains and filters) map {
            FilterRequest.FilterOperation.TimeAfter(it)
        }
        private val filter: Parser<FilterRequest.FilterOperation> =
            (fields and operations and filters) map { (field, operator, data) ->
                val operation = when (operator) {
                    Operation.Exactly -> FilterRequest.Operation.Exactly(data)
                    Operation.Contains -> FilterRequest.Operation.Contains(data)
                }
                when (field) {
                    Field.All -> FilterRequest.FilterOperation.All(operation)
                    Field.Tag -> FilterRequest.FilterOperation.Tag(operation)
                    Field.ProcessId -> FilterRequest.FilterOperation.ProcessId(operation)
                    Field.Thread -> FilterRequest.FilterOperation.Thread(operation)
                    Field.Message -> FilterRequest.FilterOperation.Message(operation)
                }
            }
        private val allFilter = OrCombinator(
            listOf(
                unquotedMaybeSavedFilter,
                filters map { FilterRequest.FilterOperation.All(FilterRequest.Operation.Contains(it)) },
            ),
        )

        private val anyFilter =
            levelFilter or filter or allFilter or runNumberFilter or timeAfterFilter or timeBeforeFilter

        private val bracedExpression: Parser<FilterRequest.FilterOperation> =
            -lpar and parser(this::autoChain) and -rpar

        private val expressionPositive = anyFilter or bracedExpression
        private val expressionNegative =
            (-(not or minus) and expressionPositive) map { FilterRequest.FilterOperation.Not(it) }
        private val expression = expressionPositive or expressionNegative

        private val andChain = leftAssociative(expression, and) { l, _, r ->
            FilterRequest.FilterOperation.And(listOf(l, r))
        }

        private val orChain = leftAssociative(andChain, or) { l, _, r ->
            FilterRequest.FilterOperation.Or(listOf(l, r))
        }

        private val autoChain = zeroOrMore(orChain) map {
            when (it.size) {
                0 -> FilterRequest.FilterOperation.NoOp
                1 -> it.first()
                else -> FilterRequest.FilterOperation.Auto(it)
            }
        }

        override val rootParser: Parser<FilterRequest.FilterOperation> = autoChain
    }

    /**
     * Имена ключевых слов грамматики, которые конфликтуют с именами сохранённых фильтров.
     */
    val reservedKeywords: Set<String> by lazy {
        grammar.tokenGroupFields.mapNotNull { (it as? LiteralToken)?.text }.toSet()
    }

    private fun highlight(request: String, tokens: Result<TokenMatchesSequence>): RequestHighlight =
        tokens.map { tokenMatchesSequence ->
            val tokens = tokenMatchesSequence.toList()
            if (tokens.any { it.type == noneMatched }) return@map RequestHighlight.InvalidSyntax(request)
            // Пропускаем игнорируемые токены (пробелы, переносы строк), что бы корректно определять предыдущий
            // значимый токен при категоризации.
            val meaningful = tokens.filter { categorize(it, prev = null) != null }.toList()
            val spans = meaningful.mapIndexed { index, token ->
                val category = categorize(token, prev = meaningful.getOrNull(index - 1))!!
                Span(IntRange(token.offset, token.offset + token.length - 1), category)
            }.toMutableList()
            addEscapeSpans(request, spans)
            RequestHighlight.Success(
                raw = request,
                spans = spans,
            )
        }.getOrElse { RequestHighlight.InvalidSyntax(request) }

    private fun categorize(token: TokenMatch, prev: TokenMatch?): Category? = when (token.type) {
        in grammar.tokenGroupFields -> Category.Field

        in grammar.tokenGroupFilterType -> Category.Operator

        in grammar.tokenGroupLogic -> {
            if (token.type == grammar.minus && prev?.type in grammar.tokenGroupFilterType) {
                Category.Text
            } else {
                Category.Logic
            }
        }

        in grammar.tokenGroupBrackets -> Category.Bracket

        in grammar.tokenGroupStringLiteral -> Category.Text

        // Слово трактуется как ссылка на сохраненный фильтр только если оно стоит отдельно (не является значением
        // выражения `поле=значение`) и совпадает с именем сохраненного фильтра.
        in grammar.tokenGroupText -> if (
            prev?.type !in grammar.tokenGroupFilterType &&
            savedFilters.value.any { it.name == token.text }
        ) {
            Category.SavedFilterRef
        } else {
            Category.Text
        }

        else -> null
    }

    private fun addEscapeSpans(request: String, spans: MutableList<Span>) {
        fun findEscapeCharsInString(text: String, offset: Int) {
            var pos = 0
            while (true) {
                pos = text.indexOf("\\\"", startIndex = pos)
                if (pos < 0) return
                spans.add(Span(offset + pos..offset + pos, Category.Escape))
                pos += 2
            }
        }

        for (span in spans.toList()) {
            if (span.category != Category.Text) continue
            val spanText = request.substring(span.range)
            if (spanText.first() == '"' && spanText.last() == '"') {
                findEscapeCharsInString(spanText, span.range.first)
            }
        }
    }

    fun justHighlight(request: String): RequestHighlight {
        val tokens = runCatching { grammar.tokenizer.tokenize(request) }
        return highlight(request, tokens)
    }

    fun parse(request: String, cursorPosition: Int = -1): ParserResult {
        val (tokens, tokenizeTime) = measureTimedValue {
            runCatching { grammar.tokenizer.tokenize(request) }
        }

        val currentTokenPredictionInfo = tokenPredict(request, cursorPosition)

        val (filterRequest, parseTime) = measureTimedValue {
            tokens.mapCatching { tokens ->
                val result = grammar.parseToEnd(tokens)
                FilterRequest(result)
            }
        }

        val highlight: RequestHighlight = highlight(request, tokens)
        FilterLogger.d { "Parsed input, tokenize=$tokenizeTime, parseTime=$parseTime, input=$request}" }

        return ParserResult(
            requestHighlight = highlight,
            searchRequest = filterRequest,
            currentTokenPredictionInfo = currentTokenPredictionInfo,
        )
    }

    /**
     * Пробует предсказать тип токена который сейчас вводится по текущей позиции курсора.
     */
    @Suppress("CyclomaticComplexMethod", "LongMethod")
    private fun tokenPredict(request: String, cursorPosition: Int): CurrentTokenPrediction? {
        val tokens =
            runCatching { grammar.tokenizer.tokenize(request.substring(0, cursorPosition)) }.getOrNull() ?: return null

        val currentTokenIndex = tokens.indexOfFirst {
            it.offset <= cursorPosition && it.offset + it.length >= cursorPosition
        }
        if (currentTokenIndex < 0) return null

        val currentToken = tokens[currentTokenIndex]!!
        val prevToken = if (currentTokenIndex > 0) tokens[currentTokenIndex - 1] else null
        val thirdToken = if (currentTokenIndex > 1) tokens[currentTokenIndex - 2] else null
        val fourthToken = if (currentTokenIndex > 2) tokens[currentTokenIndex - 3] else null

        val currentText = currentToken.text.substring(0, cursorPosition - currentToken.offset)

        TokenPredictionLogger.d { "tokenPredict(), current=$currentToken, prev=$prevToken" }

        val prediction = when {
            currentToken.isFieldGroup() && prevToken?.isFilterTypeGroup() != true -> {
                CurrentTokenPrediction(
                    startText = "",
                    type = CurrentTokenPrediction.Type.SearchType,
                )
            }

            currentToken.isFilterTypeGroup() && prevToken?.isFieldGroup() == true -> {
                when (prevToken.type) {
                    grammar.level -> CurrentTokenPrediction(
                        startText = "",
                        type = CurrentTokenPrediction.Type.LogLevel,
                    )

                    grammar.tag -> CurrentTokenPrediction(
                        startText = "",
                        type = CurrentTokenPrediction.Type.Tag,
                    )

                    grammar.runNumber -> CurrentTokenPrediction(
                        startText = "",
                        type = CurrentTokenPrediction.Type.RunNumber,
                    )

                    else -> {
                        TokenPredictionLogger.w { "Filter content prediction is not supported now" }
                        null
                    }
                }
            }

            prevToken?.type == grammar.minus && thirdToken?.isFilterTypeGroup() == true &&
                fourthToken?.isFieldGroup() == true -> {
                when (fourthToken.type) {
                    grammar.runNumber -> CurrentTokenPrediction(
                        startText = "-$currentText",
                        type = CurrentTokenPrediction.Type.RunNumber,
                    )

                    else -> {
                        TokenPredictionLogger.w { "Filter content prediction is not supported now" }
                        null
                    }
                }
            }

            prevToken == null || (!prevToken.isFilterTypeGroup() && !prevToken.isFieldGroup()) -> {
                CurrentTokenPrediction(
                    startText = currentText,
                    type = CurrentTokenPrediction.Type.Keyword,
                )
            }

            prevToken.isFieldGroup() -> {
                CurrentTokenPrediction(
                    startText = currentText,
                    type = CurrentTokenPrediction.Type.SearchType,
                )
            }

            thirdToken?.isFieldGroup() == true && prevToken.isFilterTypeGroup() -> {
                when (thirdToken.type) {
                    grammar.level -> CurrentTokenPrediction(
                        startText = currentText,
                        type = CurrentTokenPrediction.Type.LogLevel,
                    )

                    grammar.tag -> CurrentTokenPrediction(
                        startText = currentText,
                        type = CurrentTokenPrediction.Type.Tag,
                    )

                    grammar.runNumber -> CurrentTokenPrediction(
                        startText = currentText,
                        type = CurrentTokenPrediction.Type.RunNumber,
                    )

                    else -> {
                        TokenPredictionLogger.w { "Filter content prediction is not supported now" }
                        null
                    }
                }
            }

            else -> {
                TokenPredictionLogger.d { "Unknown token combination" }
                null
            }
        }

        TokenPredictionLogger.d { "Prediction $prediction" }
        return prediction
    }

    private fun TokenMatch.isFieldGroup() = type in grammar.tokenGroupFields
    private fun TokenMatch.isFilterTypeGroup() = type in grammar.tokenGroupFilterType
}
