package ru.vladislavsumin.qa.feature.rootScreen.ui.component.rootScreen

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.charleskorn.kaml.Yaml
import ru.vladislavsumin.core.decompose.components.Component
import ru.vladislavsumin.core.decompose.compose.ComposeComponent
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.Navigation
import ru.vladislavsumin.core.navigation.host.childNavigationRoot
import ru.vladislavsumin.core.ui.designSystem.theme.QaTheme
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenIntent
import ru.vladislavsumin.feature.logViewer.ui.screen.logViewer.LogViewerScreenParams
import ru.vladislavsumin.feature.windowTitle.domain.WindowTitleInteractor
import ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root.RootScreenFactory
import java.nio.file.Path

@GenerateFactory(RootScreenComponentFactory::class)
internal class RootScreenComponent(
    navigation: Navigation,
    private val yaml: Yaml,
    private val windowTitleInteractor: WindowTitleInteractor?,
    logPath: Path?,
    mappingPath: Path?,
    private val rootScreenFactory: RootScreenFactory,
    context: ComponentContext,
) : Component(context),
    ComposeComponent {

    init {
        if (logPath != null) {
            navigation.open(
                screenParams = LogViewerScreenParams(logPath),
                intent = mappingPath?.let { LogViewerScreenIntent.OpenMapping(it) },
            )
        }
    }

    private val navigationRoot = context.childNavigationRoot(
        navigation = navigation,
        customRootScreenFactory = { context, _, _ -> rootScreenFactory.create(windowTitleInteractor, context) },
    )

    @Composable
    override fun Render(modifier: Modifier) {
        QaTheme(yaml) {
            Surface {
                navigationRoot.Render(Modifier)
            }
        }
    }
}
