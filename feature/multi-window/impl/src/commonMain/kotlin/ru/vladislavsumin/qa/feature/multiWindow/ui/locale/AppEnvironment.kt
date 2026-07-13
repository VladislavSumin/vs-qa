package ru.vladislavsumin.qa.feature.multiWindow.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.key
import ru.vladislavsumin.qa.feature.settings.domain.AppLanguage

/**
 * Тег локали для передачи в [LocalAppLocale]. `null` означает системную локаль.
 */
internal fun AppLanguage.toLocaleTag(): String? = when (this) {
    AppLanguage.SYSTEM -> null
    AppLanguage.RUSSIAN -> "ru"
    AppLanguage.ENGLISH -> "en"
}

/**
 * Платформенная точка входа для переопределения локали приложения в рантайме.
 *
 * Реализация смотри в соответствующих platform source set.
 */
internal expect object LocalAppLocale {
    val current: String @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}

/**
 * Оборачивает [content] в окружение с выбранной локалью [languageTag].
 *
 * При смене [languageTag] контент пересоздается через [key], что заставляет compose ресурсы
 * перечитаться с новой локалью.
 */
@Composable
internal fun AppEnvironment(languageTag: String?, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAppLocale provides languageTag,
    ) {
        key(languageTag) {
            content()
        }
    }
}
