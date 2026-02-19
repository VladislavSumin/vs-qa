package ru.vladislavsumin.feature.logParser.anime.domain

import ru.vladislavsumin.feature.logParser.domain.LogParser
import ru.vladislavsumin.feature.logParser.domain.RawLogRecord
import java.nio.file.Path
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import kotlin.io.path.absolutePathString
import kotlin.io.path.bufferedReader
import kotlin.io.path.extension
import kotlin.system.measureTimeMillis

internal class AnimeLogParser : LogParser {
    override suspend fun parseLog(filePath: Path): List<RawLogRecord> {
        // Производительность тут примерно 1,2кк строк в секунду, поэтому дополнительные оптимизации пока не нужны.
        AnimeLogger.i { "Start parsing file $filePath with ${this.javaClass.simpleName}" }

        val result = mutableListOf<RawLogRecord>()
        val totalParseTime = measureTimeMillis {
            if (filePath.extension == "zip") {
                parseZip(filePath, result)
            } else {
                parseLogFile(filePath, result)
            }
        }
        AnimeLogger.d { "Parsed file $filePath at ${totalParseTime}ms. logs = ${result.size}}" }
        return result
    }

    @Suppress("NestedBlockDepth")
    private fun parseZip(filePath: Path, result: MutableList<RawLogRecord>) {
        val zip = ZipFile(filePath.absolutePathString())
        val names = zip.entries().toList()
            // Фильтр нужен для корректной работы с перепакованными на macos архивами.
            // Отрезаем мета информацию из архива.
            .filter { !it.isDirectory && !it.name.startsWith("__MACOSX") }
            .map { it.name }
            .sorted()

        if (names.all { it.endsWith("zip") }) {
            AnimeLogger.i { "Use embedded log parser" }
            names.forEach { name ->
                zip.getInputStream(zip.getEntry(name)).use { internalZipStream ->
                    ZipInputStream(internalZipStream).use { zipStream ->
                        val entry = zipStream.nextEntry
                        if (entry != null) {
                            val lines = zipStream.bufferedReader().lineSequence()
                            AnimeEmbeddedLogParser.parseLines(lines, result)
                        } else {
                            // TODO выводить ворнинг о нарушении формата в пользовательский интерфейс
                            AnimeLogger.e { "Unexpected empty archive $name" }
                        }
                    }
                }
            }
        } else {
            AnimeLogger.i { "Use logcat log parser" }
            names.forEach {
                zip.getInputStream(zip.getEntry(it)).use { internalZipStream ->
                    val lines = internalZipStream.bufferedReader().lineSequence()
                    AnimeLogcatLogParser.parseLines(lines, result)
                }
            }
        }
    }

    private fun parseLogFile(filePath: Path, result: MutableList<RawLogRecord>) {
        val lines = filePath.bufferedReader().lineSequence()
        AnimeEmbeddedLogParser.parseLines(lines, result)
    }
}
