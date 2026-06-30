package ru.vladislavsumin.qa.feature.rootScreen.ui.component.rootScreen

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor
import java.nio.file.Path

interface RootScreenComponentFactory {
    fun create(
        windowTitleInteractor: WindowTitleInteractor?,
        logPath: Path?,
        mappingPath: Path?,
        context: ComponentContext,
    ): ComposeComponent
}
