package ru.vladislavsumin.qa.ui.component.logViewerComponent.filterBar

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
import ru.vladislavsumin.qa.domain.logs.FilterRequest
import ru.vladislavsumin.qa.domain.logs.FilterRequest.Field
import ru.vladislavsumin.qa.domain.logs.LogLevel
import kotlin.collections.listOf

class FilterRequestParser {

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
            val field: Field,
            val operation: Operation,
            val text: String,
        ) : Filter
    }

    private val grammar = object : Grammar<List<Filter>>() {
        val tag by literalToken("tag")
        val thread by literalToken("thread")
        val message by literalToken("message")
        val level by literalToken("level")

        val timeAfter by literalToken("timeAfter")
        val timeBefore by literalToken("timeBefore")

        val exactly by literalToken(":=")
        val contains by literalToken("=")

        // Строка в кавычках, может содержать экранированные кавычки внутри
        val stingLiteral by regexToken("\"(\\\\\"|[^\"])+\"")

        // Любая строка без пробелов
        val any by regexToken("[^ ]+")

        // Пробел, определяется последним чтобы он не перебивал собой другие токены, например stringLiteral
        val ws by regexToken("\\s+", ignore = true)

        // Поля по которым можно вести поиск.
        val fields = OrCombinator(
            listOf(
                tag asJust Field.Tag,
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

        val levelFilter = (-level and -contains and filters) map { level ->
            val level = LogLevel.fromAlias(level) ?: error("Unknown level $level")
            Filter.ByLevel(level)
        }
        val timeBeforeFilter = (-timeBefore and -contains and filters) map { Filter.ByTimeBefore(it) }
        val timeAfterFilter = (-timeAfter and -contains and filters) map { Filter.ByTimeAfter(it) }
        val filter = (fields and operations and filters) map { (a, b, c) -> Filter.ByField(a, b, c) }
        val allFilter = filters map { Filter.ByField(Field.All, Operation.Contains, it) }

        override val rootParser: Parser<List<Filter>> =
            zeroOrMore(levelFilter or filter or allFilter or timeAfterFilter or timeBeforeFilter)
    }

    fun tokenize(data: String): Result<FilterRequest> = runCatching {
        val tokens = grammar.tokenizer.tokenize(data)
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

//    fun test() {
//        tokenize("")
//        tokenize("tag:=d")
//        tokenize("thread=d")
//        tokenize("thread=\"demo\"")
//        tokenize("thread=\"\\\"\"")
//        tokenize("thread=\"dem\\\"o\"")
//        tokenize("thread:=om tag=test")
//        tokenize("sdfsdfds")
//        tokenize("\"sdfsdfds\"")
//        tokenize("\"sdfs dd dfds\"")
//        tokenize("sdfsdfds sdvsvdf sdf")
//        tokenize("sdfsdfds tag:=testTag sdvsvdf sdf")
//    }
}
