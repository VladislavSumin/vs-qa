package ru.vladislavsumin.qa.feature.rootScreen.ui.component.rootScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logViewer.ui.component.logViewer.LogViewerComponentFactory
import ru.vladislavsumin.qa.feature.bottomBar.ui.component.bottomBar.BottomBarComponentFactory
import java.nio.file.Path

internal class RootScreenComponent(
    logViewerComponentFactory: LogViewerComponentFactory,
    bottomBarComponentFactory: BottomBarComponentFactory,
    logPath: Path,
    mappingPath: Path?,
    context: ComponentContext,
) : Component(context), ComposeComponent {
    private val bottomBarComponent = bottomBarComponentFactory.create(context.childContext("bottom-bar"))
    private val logViewerComponent = logViewerComponentFactory.create(
        logPath,
        mappingPath,
        bottomBarComponent.bottomBarUiInteractor,
        context.childContext("log-viewer"),
    )

    @Composable
    override fun Render(modifier: Modifier) {
        QaTheme {
            Surface {
                Column {
                    logViewerComponent.Render(Modifier.weight(1f))
                    bottomBarComponent.Render(Modifier)
                }
            }
        }
    }
}
