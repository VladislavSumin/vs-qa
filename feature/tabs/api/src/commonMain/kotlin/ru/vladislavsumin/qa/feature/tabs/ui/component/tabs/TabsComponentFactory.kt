package ru.vladislavsumin.qa.feature.tabs.ui.component.tabs

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.host.ConfigurationHolder
import ru.vladislavsumin.core.navigation.screen.Screen

interface TabsComponentFactory {
    fun create(
        pages: Value<ChildPages<ConfigurationHolder, Screen>>,
        onTabClick: (IntentScreenParams<*>) -> Unit,
        onTabClickClose: (IntentScreenParams<*>) -> Unit,
        context: ComponentContext,
    ): TabsComponent
}
