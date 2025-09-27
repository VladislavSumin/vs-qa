package ru.vladislavsumin.qa.feature.rootScreen.ui.component.rootScreen

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.navigation.Navigation
import java.nio.file.Path

internal class RootScreenComponentFactoryImpl(
    private val navigation: Navigation,
) : RootScreenComponentFactory {
    override fun create(
        logPath: Path?,
        mappingPath: Path?,
        context: ComponentContext,
    ): ComposeComponent {
        return RootScreenComponent(
            navigation,
            logPath,
            mappingPath,
            context,
        )
    }
}
