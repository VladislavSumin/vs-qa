package ru.vladislavsumin.qa.core.proguardParser

import com.github.h0tk3y.betterParse.parser.parseToEnd

object ProguardParser {
    fun parse(data: String): Result<ProguardMapping> = runCatching {
        val tokens = ProguardGrammar.tokenizer.tokenize(data)
        ProguardGrammar.parseToEnd(tokens)
    }
}
