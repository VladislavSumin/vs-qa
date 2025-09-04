package ru.vladislavsumin.qa.domain.proguard

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
import com.github.h0tk3y.betterParse.parser.parseToEnd
import java.nio.file.Path
import java.util.zip.ZipInputStream
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.readText

class ProguardParser {
    data class ProguardClass(
        val originalName: String,
        val obfuscatedName: String,
    )

    private val grammar = object : Grammar<List<ProguardClass>>() {
        val comment by regexToken("\\s*#.*", ignore = true)
        val newLine by literalToken("\n", ignore = true)

        val arrow by literalToken("->")
        val word by regexToken("[\\w.$<>\\-_сС]+") // cC это временный костыль.
        val colon by literalToken(":")

        val lpar by literalToken("(")
        val rpar by literalToken(")")
        val lsqb by literalToken("[")
        val rsqb by literalToken("]")
        val comma by literalToken(",")

        val ws by literalToken(" ", ignore = true)

        val tab = SPACES_IN_TAB times ws
        val optLeftLineNumber = 0..2 times (word and colon) // 3:4:
        val optRightLineNumber = 0..2 times (colon and word) // :3:4
        val optArray = zeroOrMore(lsqb and rsqb)
        val returnType = word and optArray
        val parameter = word and optArray and optional(comma)
        val parameters = zeroOrMore(parameter)
        val function = returnType and word and lpar and parameters and rpar
        val clazz = word and skip(arrow) and word and skip(colon)

        val mappedClazz = clazz map { (or, ob) -> ProguardClass(or.text, ob.text) }
        val field = (tab and word and optArray and word and arrow and word) asJust Unit
        val method = (tab and optLeftLineNumber and function and optRightLineNumber and arrow and word) asJust Unit

        override val rootParser: Parser<List<ProguardClass>> =
            zeroOrMore((mappedClazz and zeroOrMore(field or method)) map { (clazz, members) -> clazz })
    }

    fun parse(path: Path): Result<List<ProguardClass>> {
        return if (path.extension == "zip") {
            ZipInputStream(path.inputStream()).use { zip ->
                zip.nextEntry
                val text = zip.bufferedReader().readText()
                parse(text)
            }
        } else {
            parse(path.readText())
        }
    }

    private fun parse(data: String): Result<List<ProguardClass>> = runCatching {
        val tokens = grammar.tokenizer.tokenize(data)
        grammar.parseToEnd(tokens)
    }

    private companion object {
        private const val SPACES_IN_TAB = 4
    }
}
