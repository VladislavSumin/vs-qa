package ru.vladislavsumin.qa.feature.legalInfo

import org.kodein.di.DI
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.navigation.registration.bindGenericNavigation
import ru.vladislavsumin.qa.feature.legalInfo.ui.screen.NavigationRegistrarImpl
import ru.vladislavsumin.qa.feature.legalInfo.ui.screen.legalInfo.LegalInfoScreenFactory

fun Modules.featureLegalInfo() = DI.Module("feature-legalInfo") {
    bindGenericNavigation {
        NavigationRegistrarImpl(LegalInfoScreenFactory())
    }
}
