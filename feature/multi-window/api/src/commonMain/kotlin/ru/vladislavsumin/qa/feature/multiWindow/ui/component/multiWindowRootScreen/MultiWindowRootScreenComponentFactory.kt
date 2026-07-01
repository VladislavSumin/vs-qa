package ru.vladislavsumin.qa.feature.multiWindow.ui.component.multiWindowRootScreen

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import java.nio.file.Path

interface MultiWindowRootScreenComponentFactory {
    fun create(logPath: Path?, mappingPath: Path?, context: ComponentContext): ComposeComponent
}
