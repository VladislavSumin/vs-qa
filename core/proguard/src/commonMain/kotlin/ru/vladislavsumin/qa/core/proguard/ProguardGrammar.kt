package ru.vladislavsumin.qa.core.proguard

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.asJust
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.skip
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

/**
 * Содержит в себе грамматику для чтения mapping.txt генерируемых proguard
 */
@Suppress("UnusedPrivateProperty") // часть синтаксиса Grammar
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
    private val optLeftLineNumber = 0..2 times (word and colon) // 3:4:
    private val optRightLineNumber = 0..2 times (colon and word) // :3:4
    private val optArray = zeroOrMore(lsqb and rsqb)
    private val returnType = word and optArray
    private val parameter = word and optArray and optional(comma)
    private val parameters = zeroOrMore(parameter)
    private val function = returnType and word and lpar and parameters and rpar
    private val clazz = word and skip(arrow) and word and skip(colon)

    private val mappedClazz = clazz map { (or, ob) -> ProguardClass(or.text, ob.text) }
    private val field = (tab and word and optArray and word and arrow and word) asJust Unit
    private val method = (tab and optLeftLineNumber and function and optRightLineNumber and arrow and word) asJust Unit
    private val proguardClass = (mappedClazz and zeroOrMore(field or method)) map { (clazz, members) -> clazz }

    override val rootParser: Parser<List<ProguardClass>> = zeroOrMore(proguardClass)

    private const val SPACES_IN_TAB = 4
}
