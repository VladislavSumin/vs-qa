@file:Suppress("INVISIBLE_REFERENCE")

package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerScopeImpl
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier

/**
 * Кастомная замена [HorizontalPager], убирающая ВСЕ механизмы скролла.
 *
 * ## Решает проблему
 *
 * При выделении текста мышью в [SelectionContainer] внутри страницы и уводе курсора далеко
 * влево за границы контейнера — [HorizontalPager] переключает страницу, несмотря на
 * `userScrollEnabled = false`. Таб-бар не знает о переключении (`onPageSelected = {}`),
 * навигация Decompose не обновляется — визуальный рассинхрон.
 *
 * ВНИМАНИЕ: реализация JVM-only — на Android internal API `PagerState` отсутствуют в рантайме
 * (другая версия `androidx.compose.foundation`) и падают с `NoSuchMethodError`.
 */
@Composable
internal actual fun NonScrollablePager(
    modifier: Modifier,
    state: PagerState,
    key: ((Int) -> Any)?,
    pageContent: @Composable PagerScope.(Int) -> Unit,
    expectedPage: Int,
) {
    val holder = rememberSaveableStateHolder()
    val displayPage = if (state.currentPage == expectedPage) state.currentPage else expectedPage
    val pageKey = key?.invoke(displayPage) ?: displayPage as Any

    Box(
        modifier
            .then(state.awaitLayoutModifier)
            .then(state.remeasurementModifier),
    ) {
        holder.SaveableStateProvider(pageKey) {
            PagerScopeImpl.pageContent(displayPage)
        }
    }
}
