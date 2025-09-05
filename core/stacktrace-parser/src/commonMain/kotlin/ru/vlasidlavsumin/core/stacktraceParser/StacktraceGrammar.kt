package ru.vlasidlavsumin.core.stacktraceParser

import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.oneOrMore
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

@Suppress("UnusedPrivateProperty") // часть синтаксиса Grammar
internal object StacktraceGrammar : Grammar<StackTrace>() {
    private val at by literalToken("at")
    private val unknownSource by literalToken("Unknown Source")
    private val word by regexToken("[\\w.$<>\\-_]+")
    private val message by regexToken(": .*\n") // такое себе предположение, но для старта ок.

    private val tab by literalToken("\t")
    private val lpar by literalToken("(")
    private val rpar by literalToken(")")
    private val colon by literalToken(":")

    private val ws by literalToken(" ", ignore = true)
    private val newLine by literalToken("\n", ignore = true)

    private val clearMessage by message map { it.text.substring(1, it.text.length - 1) }
    private val header by word * optional(clearMessage)
    private val element by (-tab * -at * word * -lpar * (word or unknownSource) * -colon * word * -rpar) map
        { (methodWithClass, file, line) ->
            val clazz = methodWithClass.text.substringBeforeLast(".")
            val method = methodWithClass.text.substringAfterLast(".")
            StackTrace.Element(
                clazz = clazz,
                method = method,
                file = file.text,
                line = line.text,
            )
        }
    private val stack by (header * oneOrMore(element)) map { (header, elements) ->
        StackTrace(
            clazz = header.t1.text,
            message = header.t2,
            elements = elements,
        )
    }

    override val rootParser: Parser<StackTrace> = stack
}
