package ru.vladislavsumin.qa.domain.proguard

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.asJust
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import java.nio.file.Path
import java.util.zip.ZipInputStream
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.inputStream
import kotlin.io.path.readText


class ProguardParser() {
    sealed interface ProguardRecord
    data class ProguardClass(
        val originalName: String,
        val obfuscatedName: String,
    ) : ProguardRecord

    data object Unsupported : ProguardRecord

    private val grammar = object : Grammar<Any>() {
        val comment by regexToken("\\s*#.*", ignore = true)
        val newLine by literalToken("\n", ignore = true)

        val arrow by literalToken("->")
        val word by regexToken("[\\w.$<>\\-_]+")
        val colon by literalToken(":")

        val lpar by literalToken("(")
        val rpar by literalToken(")")
        val lsqb by literalToken("[")
        val rsqb by literalToken("]")
        val comma by literalToken(",")

        val ws by literalToken(" ", ignore = true)

        val clazz = (word and arrow and word and colon) map { (or, _, ob, _) -> ProguardClass(or.text, ob.text) }
        val field =
            ((4 times ws) and word and zeroOrMore(lsqb and rsqb) and word and arrow and word) asJust Unsupported
        val method =
            ((4 times ws) and optional(word and colon and word and colon) and word and zeroOrMore(lsqb and rsqb) and word and lpar and zeroOrMore(
                word and zeroOrMore(lsqb and rsqb) and optional(comma)
            ) and rpar and optional(colon and word) and optional(colon and word) and arrow and word) asJust Unsupported

        override val rootParser: Parser<Any> =
            zeroOrMore((clazz and zeroOrMore(field or method)) map { (clazz, members) -> clazz })
    }

    fun parse(path: Path) {
        if (path.extension == "zip") {
            ZipInputStream(path.inputStream()).use { zip ->
                zip.nextEntry
                val text = zip.bufferedReader().readText()
                parse(text)
            }
        } else {
            parse(path.readText())
        }
    }


    fun parse(data: String) {
        val tokens = grammar.tokenizer.tokenize(data)
        //println(tokens.joinToString(separator = "\n"))
//        println()
//        println()
        val data = grammar.parseToEnd(tokens)
        println(data)
    }
}

fun main() {
    //    5853:5917:int DebugStringsKt.getBitrate(java.lang.String,int,int,float):0 -> s
    //    0:3:long ru.ok.tamtam.chats.ChatExtKt.getChatReadMark(ru.ok.tamtam.chats.Chat):5:5 -> t
//    ProguardParser().parse("""
//        #
//            boolean isExpanded -> z0
//            0:10:void <clinit>():49:49 -> <clinit>
//    """.trimIndent())
    ProguardParser().parse(Path("../patched-mapping.txt"))
}