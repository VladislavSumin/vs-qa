package ru.vladislavsumin.qa.core.proguardParser

import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

/**
 * Содержит в себе грамматику для чтения mapping.txt генерируемых proguard
 */
@Suppress("UnusedPrivateProperty", "DestructuringDeclarationWithTooManyEntries") // часть синтаксиса Grammar
internal object ProguardGrammar : Grammar<List<ProguardClass>>() {
    private val comment by regexToken("\\s*#.*", ignore = true)
    private val newLine by literalToken("\n", ignore = true)

    private val arrow by literalToken("->")
    private val word by regexToken("[\\w.$<>\\-_сС]+") // cC это временный костыль.
    private val colon by literalToken(":")

    private val lpar by literalToken("(")
    private val rpar by literalToken(")")
    private val lsqb by literalToken("[")
    private val rsqb by literalToken("]")
    private val comma by literalToken(",")

    private val ws by literalToken(" ", ignore = true)

    private val tab = SPACES_IN_TAB times ws
    private val optLLineNumber = 0..2 times (word * colon) // 3:4:
    private val optRLineNumber = 0..2 times (colon * word) // :3:4
    private val optArray = zeroOrMore(lsqb * rsqb) map { "[]".repeat(it.size) }
    private val type = word * optArray * -optional(comma) map { (type, array) -> type.text + array }
    private val parameters = zeroOrMore(type)
    private val function = type * word * -lpar * parameters * -rpar map {
        val (type, name, params) = it
        Triple(type, name.text, params)
    }
    private val clazz = word * -arrow * word * -colon map { (or, ob) -> or.text to ob.text }

    private val field = (-tab * type * word * -arrow * word) map { (type, or, ob) ->
        ProguardClass.ProguardField(
            originalName = or.text,
            obfuscatedName = ob.text,
            type = type,
        )
    }
    private val method = (-tab * -optLLineNumber * function * -optRLineNumber * -arrow * word) map {
        val (function, ob) = it
        val (type, or, params) = function
        ProguardClass.ProguardMethod(
            originalName = or,
            obfuscatedName = ob.text,
            returnType = type,
        )
    }
    private val proguardClass = (clazz * zeroOrMore(field or method)) map { (clazz, members) ->
        ProguardClass(
            originalName = clazz.first,
            obfuscatedName = clazz.second,
            fields = members.filterIsInstance<ProguardClass.ProguardField>(),
            methods = members.filterIsInstance<ProguardClass.ProguardMethod>(),
        )
    }

    override val rootParser: Parser<List<ProguardClass>> = zeroOrMore(proguardClass)

    private const val SPACES_IN_TAB = 4
}
