package ru.vladislavsumin.feature.logViewer.ui.component.filterBar

import com.github.h0tk3y.betterParse.combinators.OrCombinator
import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.asJust
import com.github.h0tk3y.betterParse.combinators.leftAssociative
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import ru.vladislavsumin.core.logger.api.logger
import ru.vladislavsumin.core.utils.measureTimeMillisWithResult
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

    private enum class Field {
        All,

        Tag,
        ProcessId,
        Thread,
        Message,
    }

    @Suppress("UnusedPrivateProperty")
    private val grammar = object : Grammar<FilterRequest.FilterOperation>() {
        private val tag by literalToken("tag")
        private val pid by literalToken("pid")
        private val tid by literalToken("tid")
        private val thread by literalToken("thread")
        private val message by literalToken("message")
        private val level by literalToken("level")

        private val runNumber by literalToken("runNumber")
        private val timeAfter by literalToken("timeAfter")
        private val timeBefore by literalToken("timeBefore")

        private val exactly by literalToken(":=")
        private val contains by literalToken("=")

        private val not by literalToken("!")
        private val minus by literalToken("-")

        private val and by literalToken("&")
        private val or by literalToken("|")

        private val lpar by literalToken("(")
        private val rpar by literalToken(")")

        // Строка в кавычках, может содержать экранированные кавычки внутри
        private val stingLiteral by regexToken("\"(\\\\\"|[^\"])+\"")

        // Любая строка без пробелов
        private val any by regexToken("[^ \n()]+")

        // Определяются последними чтобы не перебивать собой другие токены, например stringLiteral.
        private val ws by regexToken("\\s+", ignore = true)
        private val newLine by literalToken("\n", ignore = true)

        val keywords = setOf(
            tag, pid, tid, thread, message, level,
            runNumber, timeAfter, timeBefore, exactly,
            contains, not, minus,
            and, or, lpar, rpar,
        )
        val data = setOf(stingLiteral, any)

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
            val level = LogLevel.fromAlias(level) ?: error("Unknown level $level")
            FilterRequest.FilterOperation.MinLogLevel(level)
        }
        private val runNumberFilter = (-runNumber and -contains and filters) map {
            FilterRequest.FilterOperation.RunNumber(it.toInt() - 1)
        }
        private val timeBeforeFilter = (-timeBefore and -contains and filters) map {
            FilterRequest.FilterOperation.TimeBefore(it)
        }
        private val timeAfterFilter = (-timeAfter and -contains and filters) map {
            FilterRequest.FilterOperation.TimeAfter(it)
        }
        private val filter: Parser<FilterRequest.FilterOperation> =
            (fields and operations and filters) map { (a, b, c) ->
                val operation = when (b) {
                    Operation.Exactly -> FilterRequest.Operation.Exactly(c)
                    Operation.Contains -> FilterRequest.Operation.Contains(c)
                }
                when (a) {
                    Field.All -> FilterRequest.FilterOperation.All(operation)
                    Field.Tag -> FilterRequest.FilterOperation.Tag(operation)
                    Field.ProcessId -> FilterRequest.FilterOperation.ProcessId(operation)
                    Field.Thread -> FilterRequest.FilterOperation.Thread(operation)
                    Field.Message -> FilterRequest.FilterOperation.Message(operation)
                }
            }
        private val allFilter = filters map {
            FilterRequest.FilterOperation.All(FilterRequest.Operation.Contains(it))
        }
        private val anyPositiveFilter =
            levelFilter or filter or allFilter or runNumberFilter or timeAfterFilter or timeBeforeFilter
        private val anyNotFilter = (-(not or minus) and anyPositiveFilter) map { FilterRequest.FilterOperation.Not(it) }

        private val anyFilter = anyPositiveFilter or anyNotFilter

        private val bracedExpression: Parser<FilterRequest.FilterOperation> =
            -lpar and parser(this::autoChain) and -rpar

        private val expression = anyFilter or bracedExpression

        private val andChain = leftAssociative(expression, and) { l, _, r ->
            FilterRequest.FilterOperation.And(listOf(l, r))
        }

        private val orChain = leftAssociative(andChain, or) { l, _, r ->
            FilterRequest.FilterOperation.Or(listOf(l, r))
        }

        private val autoChain = zeroOrMore(orChain) map {
            FilterRequest.FilterOperation.Auto(it)
        }

        override val rootParser: Parser<FilterRequest.FilterOperation> = autoChain
    }

    fun tokenize(request: String): ParserResult {
        val (tokenizeTime, tokens) = measureTimeMillisWithResult {
            runCatching { grammar.tokenizer.tokenize(request) }
        }

        val (parseTime, filterRequest) = measureTimeMillisWithResult {
            tokens.mapCatching { tokens ->
                val result = grammar.parseToEnd(tokens)
                FilterRequest(result)
            }
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

        FilterLogger.d {
            "Parsed input, tokenize=${tokenizeTime}ms, parseTime=${parseTime}ms, input=$request"
        }

        return ParserResult(
            requestHighlight = highlight,
            searchRequest = filterRequest,
        )
    }
}

private val FilterLogger = logger("Filter")
