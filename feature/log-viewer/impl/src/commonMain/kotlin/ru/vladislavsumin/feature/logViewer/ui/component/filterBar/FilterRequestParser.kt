package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import com.github.h0tk3y.betterParse.combinators.OrCombinator
import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.asJust
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import ru.vladislavsumin.feature.logParser.domain.LogLevel
import ru.vladislavsumin.feature.logViewer.domain.logs.FilterRequest

class FilterRequestParser {

    data class ParserResult(
        val requestHighlight: RequestHighlight,
        val searchRequest: Result<FilterRequest>,
    )

    sealed interface RequestHighlight {
        val raw: String

        data class Success(
            override val raw: String,
            val keywords: List<IntRange>,
            val data: List<IntRange>,
        ) : RequestHighlight

        data class InvalidSyntax(
            override val raw: String,
        ) : RequestHighlight
    }

    private enum class Operation {
        Exactly,
        Contains,
    }

    private sealed interface Filter {
        data class ByLevel(
            val level: LogLevel,
        ) : Filter

        data class ByTimeBefore(
            val time: String,
        ) : Filter

        data class ByTimeAfter(
            val time: String,
        ) : Filter

        data class ByField(
            val field: FilterRequest.Field,
            val operation: Operation,
            val text: String,
        ) : Filter
    }

    private val grammar = object : Grammar<List<Filter>>() {
        private val tag by literalToken("tag")
        private val thread by literalToken("thread")
        private val message by literalToken("message")
        private val level by literalToken("level")

        private val timeAfter by literalToken("timeAfter")
        private val timeBefore by literalToken("timeBefore")

        private val exactly by literalToken(":=")
        private val contains by literalToken("=")

        // Строка в кавычках, может содержать экранированные кавычки внутри
        private val stingLiteral by regexToken("\"(\\\\\"|[^\"])+\"")

        // Любая строка без пробелов
        private val any by regexToken("[^ ]+")

        // Пробел, определяется последним чтобы он не перебивал собой другие токены, например stringLiteral
        @Suppress("UnusedPrivateProperty")
        private val ws by regexToken("\\s+", ignore = true)

        val keywords = setOf(tag, thread, message, level, timeAfter, timeBefore, exactly, contains)
        val data = setOf(stingLiteral, any)

        // Поля по которым можно вести поиск.
        val fields = OrCombinator(
            listOf(
                tag asJust FilterRequest.Field.Tag,
                thread asJust FilterRequest.Field.Thread,
                message asJust FilterRequest.Field.Message,
            ),
        )

        // Операции поиска
        val operations = OrCombinator(
            listOf(
                exactly asJust Operation.Exactly,
                contains asJust Operation.Contains,
            ),
        )

        // Фильтры
        val filters = OrCombinator(
            listOf(
                stingLiteral map {
                    it.text
                        .substring(1, it.length - 1) // отрезаем внешние кавычки
                        .replace("\\\"", "\"") // убираем экраны с внутренних кавычек
                },
                any map { it.text },
            ),
        )

        private val levelFilter = (-level and -contains and filters) map { level ->
            val level = LogLevel.Companion.fromAlias(level) ?: error("Unknown level $level")
            Filter.ByLevel(level)
        }
        private val timeBeforeFilter = (-timeBefore and -contains and filters) map { Filter.ByTimeBefore(it) }
        private val timeAfterFilter = (-timeAfter and -contains and filters) map { Filter.ByTimeAfter(it) }
        private val filter = (fields and operations and filters) map { (a, b, c) -> Filter.ByField(a, b, c) }
        private val allFilter = filters map { Filter.ByField(FilterRequest.Field.All, Operation.Contains, it) }

        override val rootParser: Parser<List<Filter>> =
            zeroOrMore(levelFilter or filter or allFilter or timeAfterFilter or timeBeforeFilter)
    }

    fun tokenize(request: String): ParserResult {
        val tokens = runCatching { grammar.tokenizer.tokenize(request) }

        val filterRequest = tokens.mapCatching { tokens ->
            val result = grammar.parseToEnd(tokens)
            val level = result
                .filterIsInstance<Filter.ByLevel>()
                .also { check(it.size < 2) { "More then one level filter defined" } }
                .singleOrNull()

            val timeBefore = result
                .filterIsInstance<Filter.ByTimeBefore>()
                .also { check(it.size < 2) { "More then one timeBefore filter defined" } }
                .singleOrNull()

            val timeAfter = result
                .filterIsInstance<Filter.ByTimeAfter>()
                .also { check(it.size < 2) { "More then one timeAfter filter defined" } }
                .singleOrNull()

            val filters = result
                .filterIsInstance<Filter.ByField>()
                .groupBy { it.field }
                .mapValues { (_, v) ->
                    v.map {
                        when (it.operation) {
                            Operation.Exactly -> FilterRequest.Operation.Exactly(it.text)
                            Operation.Contains -> FilterRequest.Operation.Contains(it.text)
                        }
                    }
                }
            FilterRequest(
                minLevel = level?.level,
                filters = filters,
                timeBefore = timeBefore?.time,
                timeAfter = timeAfter?.time,
            )
        }

        val highlight: RequestHighlight = tokens
            .map { tokens ->
                val keywords = tokens
                    .filter { it.type in grammar.keywords }
                    .map { IntRange(it.offset, it.offset + it.length - 1) }
                    .toList()
                val data = tokens
                    .filter { it.type in grammar.data }
                    .map { IntRange(it.offset, it.offset + it.length - 1) }
                    .toList()
                RequestHighlight.Success(
                    raw = request,
                    keywords = keywords,
                    data = data,
                )
            }
            .getOrElse { RequestHighlight.InvalidSyntax(request) }

        return ParserResult(
            requestHighlight = highlight,
            searchRequest = filterRequest,
        )
    }
}
