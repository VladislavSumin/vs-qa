package ru.vlasidlavsumin.core.stacktraceParser

import com.github.h0tk3y.betterParse.parser.parseToEnd

object StacktraceParser {
    fun parse(data: String): Result<StackTrace> = runCatching {
        val tokens = StacktraceGrammar.tokenizer.tokenize(data)
        StacktraceGrammar.parseToEnd(tokens)
    }
}
