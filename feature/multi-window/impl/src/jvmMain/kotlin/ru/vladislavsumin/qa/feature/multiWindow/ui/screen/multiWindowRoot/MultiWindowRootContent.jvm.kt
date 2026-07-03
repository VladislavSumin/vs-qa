package ru.vladislavsumin.qa.feature.multiWindow.ui.screen.multiWindowRoot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.GenericScreen

@Composable
internal actual fun MultiWindowRootContent(
    windows: Value<ChildPages<ConfigurationHolder, GenericScreen<ComponentContext>>>,
    modifier: Modifier,
) {
    val windows by windows.subscribeAsState()
    for (child in windows.items) {
        // Key нужен что бы композ корретно обрабатывал ситуацию [1,2] -> [2] удаление младшего элемента.
        key(child.configuration) {
            child.instance?.Render(Modifier)
        }
    }
}
