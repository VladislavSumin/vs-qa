package ru.vladislavsumin.qa.ui.component.logViewerComponent.filterBar

import com.github.h0tk3y.betterParse.combinators.OrCombinator
import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.asJust
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import kotlin.collections.listOf

class FilterRequestParser {

    enum class Field {
        Tag,
        Thread,
        Message,
    }

    enum class Operation {
        Exactly,
        Contains,
    }

    data class Filter(
        val field: Field,
        val operation: Operation,
        val text: String,
    )

    fun tokenize(data: String) {
        val grammar = object : Grammar<Any>() {
            val tag by literalToken("tag")
            val thread by literalToken("thread")
            val message by literalToken("message")

            val exactly by literalToken(":=")
            val contains by literalToken("=")
            val stingLiteral by regexToken("\"(\\\\\"|[^\"])+\"")
            val any by regexToken("[^ ]+")
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

            val filter = (fields and operations and filters) map { (a, b, c) -> Filter(a, b, c) }

            override val rootParser: Parser<Any> = zeroOrMore(filter)
        }

        val tokens = grammar.tokenizer.tokenize(data)
        val result = grammar.parseToEnd(tokens)
        println("QWQW result = $result")
    }

    fun test() {
        tokenize("")
        tokenize("tag:=d")
        tokenize("thread=d")
        tokenize("thread=\"demo\"")
        tokenize("thread=\"\\\"\"")
        tokenize("thread=\"dem\\\"o\"")
        tokenize("thread:=om tag=test")
    }
}
