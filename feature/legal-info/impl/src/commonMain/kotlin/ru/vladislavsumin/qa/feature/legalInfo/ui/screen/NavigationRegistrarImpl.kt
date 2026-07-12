package ru.vladislavsumin.qa.feature.legalInfo.ui.screen

import com.arkivanov.decompose.ComponentContext
import ru.vladislavsumin.core.navigation.registration.NavigationRegistrar
import ru.vladislavsumin.core.navigation.registration.NavigationRegistry
import ru.vladislavsumin.qa.feature.legalInfo.ui.screen.legalInfo.LegalInfoScreenFactory
import ru.vladislavsumin.qa.feature.legalInfo.ui.screen.legalInfo.LegalInfoScreenParams

internal class NavigationRegistrarImpl(private val legalInfoScreenFactory: LegalInfoScreenFactory) :
    NavigationRegistrar {
    override fun NavigationRegistry<ComponentContext>.register() {
        registerScreen(
            factory = legalInfoScreenFactory,
            defaultParams = LegalInfoScreenParams,
            description = "Правовая информация",
        )
    }
}
