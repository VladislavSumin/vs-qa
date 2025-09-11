package ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar

import com.arkivanov.decompose.ComponentContext

interface BottomBarComponentFactory {
    fun create(context: ComponentContext): BottomBarComponent
}
