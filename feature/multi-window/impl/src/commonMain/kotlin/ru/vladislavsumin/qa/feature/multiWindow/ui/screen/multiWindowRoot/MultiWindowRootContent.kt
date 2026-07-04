package ru.vladislavsumin.qa.feature.multiWindow.ui.screen.multiWindowRoot

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.GenericScreen

@Composable
internal expect fun MultiWindowRootContent(
    windows: Value<ChildPages<ConfigurationHolder, GenericScreen<ComponentContext>>>,
    modifier: Modifier,
)
