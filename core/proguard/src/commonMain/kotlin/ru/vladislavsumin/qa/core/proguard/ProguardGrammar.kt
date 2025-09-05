package ru.vladislavsumin.qa.core.proguard

import com.github.h0tk3y.betterParse.combinators.asJust
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
    private val optLeftLineNumber = 0..2 times (word * colon) // 3:4:
    private val optRightLineNumber = 0..2 times (colon * word) // :3:4
    private val optArray = zeroOrMore(lsqb * rsqb)
    private val returnType = word * optArray
    private val parameter = word * optArray * optional(comma)
    private val parameters = zeroOrMore(parameter)
    private val function = returnType * word * lpar * parameters * rpar
    private val clazz = word * -arrow * word * -colon

    private val mappedClazz = clazz map { (or, ob) -> ProguardClass(or.text, ob.text) }
    private val field = (tab * word * optArray * word * arrow * word) asJust Unit
    private val method = (tab * optLeftLineNumber * function * optRightLineNumber * arrow * word) asJust Unit
    private val proguardClass = (mappedClazz * zeroOrMore(field or method)) map { (clazz, members) -> clazz }

    override val rootParser: Parser<List<ProguardClass>> = zeroOrMore(proguardClass)

    private const val SPACES_IN_TAB = 4
}
