package ru.vladislavsumin.qa.feature.multiWindow.ui.screen.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.GenericScreen

@Composable
internal expect fun WindowContent(
    screen: Value<ChildSlot<ConfigurationHolder, GenericScreen<ComponentContext>>>,
    modifier: Modifier,
)
