package ru.vladislavsumin.qa.feature.rootScreen.ui.component.rootScreen

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.core.navigation.host.childNavigationRoot
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams
import java.nio.file.Path

internal class RootScreenComponent(
    navigation: Navigation,
    logPath: Path,
    mappingPath: Path?,
    context: ComponentContext,
) : Component(context), ComposeComponent {

    init {
        navigation.open(LogViewerScreenParams(logPath, mappingPath))
    }

    private val navigationRoot = context.childNavigationRoot(navigation)

    @Composable
    override fun Render(modifier: Modifier) {
        QaTheme {
            Surface {
                navigationRoot.Render(Modifier)
            }
        }
    }
}
