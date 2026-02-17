package ru.vladislavsumin.feature.logViewer.ui.component.filterHint

/**
 * Предсказание текущего токена
 */
internal data class CurrentTokenPrediction(
    val startText: String,
    val type: Type,
) {
    enum class Type {
        /**
         * Ключевое слово (teg, message, etc)
         */
        Keyword,

        /**
         * Тип поиска (=, :=, etc)
         */
        SearchType,
    }
}
