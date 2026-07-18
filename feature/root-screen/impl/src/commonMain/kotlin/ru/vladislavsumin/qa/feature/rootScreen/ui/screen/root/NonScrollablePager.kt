package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Замена [androidx.compose.foundation.pager.HorizontalPager] для табов, у которой пользовательский
 * скролл не работает ни при каких условиях.
 *
 * Реализации платформозависимые:
 * - **JVM** — кастомный `Box` без pager-механики вовсе: чинит переключение табов при уводе
 *   выделения текста мышью за границы контейнера (см. KDoc в `NonScrollablePager.jvm.kt`).
 * - **Android** — обычный `HorizontalPager(userScrollEnabled = false)`: проблема выделения мышью
 *   не актуальна, а JVM-реализация использует internal API `PagerState`, которых нет в рантаймовой
 *   версии `androidx.compose.foundation` на Android (падает с [NoSuchMethodError]).
 *
 * @param expectedPage актуальная страница из Decompose (`selectedIndex`), используется JVM-реализацией.
 */
@Composable
internal expect fun NonScrollablePager(
    modifier: Modifier,
    state: PagerState,
    key: ((Int) -> Any)?,
    pageContent: @Composable PagerScope.(Int) -> Unit,
    expectedPage: Int,
)
