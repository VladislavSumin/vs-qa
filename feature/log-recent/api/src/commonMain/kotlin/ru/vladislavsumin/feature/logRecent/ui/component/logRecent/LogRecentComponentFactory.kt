package ru.vladislavsumin.feature.logRecent.ui.component.logRecent

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent

interface LogRecentComponentFactory {
    fun create(context: ComponentContext): ComposeComponent
}
