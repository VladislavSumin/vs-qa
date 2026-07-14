@file:Suppress("INVISIBLE_REFERENCE")

package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerScopeImpl
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.Screen

@Composable
internal fun RootContent(
    tabs: Value<ChildPages<ConfigurationHolder, Screen>>,
    tabsComponent: ComposeComponent,
    bottomBarComponent: ComposeComponent,
    notificationsComponent: ComposeComponent,
    modifier: Modifier,
) {
    Box(
        modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        Column(modifier) {
            Box(Modifier.weight(1f)) {
                Column {
                    tabsComponent.Render(Modifier.fillMaxWidth())
                    TabsContent(tabs)
                }
                notificationsComponent.Render(
                    Modifier
                        .padding(bottom = 48.dp, end = 48.dp)
                        .align(Alignment.BottomEnd),
                )
            }
            bottomBarComponent.Render(Modifier)
        }
    }
}

@Composable
private fun ColumnScope.TabsContent(tabs: Value<ChildPages<ConfigurationHolder, Screen>>) {
    val tabsState by tabs.subscribeAsState()
    val expectedPage = tabsState.selectedIndex.coerceAtLeast(0)

    ChildPages(
        pages = tabs,
        pager = { modifier, state, key, pageContent ->
            NonScrollablePager(modifier, state, key, pageContent, expectedPage)
        },
        onPageSelected = { _ -> },
        modifier = Modifier.weight(1f),
    ) { _, page ->
        val hackyContent = remember(page) {
            movableContentOf {
                page.Render(Modifier)
            }
        }
        hackyContent()
    }
}

/**
 * Кастомная замена [HorizontalPager], убирающая ВСЕ механизмы скролла.
 *
 * ## Проблема
 *
 * При выделении текста мышью в [SelectionContainer] внутри страницы и уводе курсора далеко
 * влево за границы контейнера — [HorizontalPager] переключает страницу, несмотря на
 * `userScrollEnabled = false`. Таб-бар не знает о переключении (`onPageSelected = {}`),
 * навигация Decompose не обновляется — визуальный рассинхрон.
 *
 * ## Что перепробовали (НЕ сработало)
 *
 * | Подход | Результат |
 * |--------|-----------|
 * | `NestedScrollConnection` внутри `LogsContent` | Не видит горизонтальных событий |
 * | `NestedScrollConnection` на `ChildPages` | Табы не переключаются, но бесконечный флуд |
 * | `pageNestedScrollConnection` на `HorizontalPager` | Не видит горизонтальных событий |
 * | Dummy `ScrollableState` между `SelectionContainer` и pager'ом | Табы не переключаются, но бесконечный флуд |
 * | `NestedScrollConnection` без `onPreFling` | Бесконечный флуд |
 * | `NestedScrollConnection` с потреблением 99.9% вместо 100% | Медленно едет + бесконечный флуд |
 * | `NestedScrollConnection` (100%) + no-op `flingBehavior` | Бесконечный флуд |
 * | `pointerInput` в `VsSelectionContainer` (Main-pass) | Не видит событий |
 * | `pointerInput` в `VsSelectionContainer` (Initial-pass с consume) | Ломает выделение |
 * | `pointerInput` в `VsSelectionContainer` (Initial-pass без consume) | Не влияет |
 * | `LaunchedEffect` + `scrollToPage` для коррекции | Дёргается туда-сюда, ломает выделение |
 *
 * ## Решение
 *
 * Не `HorizontalPager` — чистый `Box` с ручной композицией страниц.
 *
 * - `state.awaitLayoutModifier` / `state.remeasurementModifier` — внутренние модификаторы
 *   `PagerState`, без которых `scrollToPage()` из Decompose зависает на `awaitFirstLayout()`.
 * - `PagerScopeImpl` — internal-синглтон `PagerScope`, доступен через
 *   `@Suppress("INVISIBLE_REFERENCE")` (уже используется в проекте).
 * - `rememberSaveableStateHolder` + `SaveableStateProvider(key)` — штатное Compose API
 *   для сохранения `rememberSaveable`-состояния при переключении страниц (скролл-позиции и т.д.).
 * - `expectedPage` = `tabsState.selectedIndex` — один Int из Decompose для предотвращения
 *   мерцания при reorder табов (аналог `LazyLayoutKeyIndexMap.findIndexByKey()` в библиотечном
 *   пейджере, только вместо 200 строк `LazyLayout`-инфраструктуры).
 *
 * Никаких `ScrollableState`, `scrollableArea`, `dragDirectionDetector`, `nestedScroll`.
 * Проблема решена на корню.
 */

@Composable
private fun NonScrollablePager(
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
