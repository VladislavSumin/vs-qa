package ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar

import ru.vladislavsumin.core.decompose.compose.ComposeComponent

interface BottomBarComponent : ComposeComponent {
    val bottomBarUiInteractor: BottomBarUiInteractor
}
