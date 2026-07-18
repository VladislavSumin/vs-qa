package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * На Android используем обычный [HorizontalPager]: баг с переключением табов при выделении
 * текста мышью здесь не актуален, а JVM-реализация завязана на internal API [PagerState]
 * (`awaitLayoutModifier`, `remeasurementModifier`, `PagerScopeImpl`), которых нет в рантаймовой
 * версии `androidx.compose.foundation` — приложение падает с [NoSuchMethodError].
 */
@Composable
internal actual fun NonScrollablePager(
    modifier: Modifier,
    state: PagerState,
    key: ((Int) -> Any)?,
    pageContent: @Composable PagerScope.(Int) -> Unit,
    @Suppress("UNUSED_PARAMETER") expectedPage: Int,
) {
    HorizontalPager(
        modifier = modifier,
        state = state,
        key = key,
        userScrollEnabled = false,
        pageContent = pageContent,
    )
}
