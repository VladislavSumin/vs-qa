package ru.vladislavsumin.qa.feature.debug

import org.kodein.di.DI
import ru.vladislavsumin.core.di.Modules
import ru.vladislavsumin.core.di.i
import ru.vladislavsumin.core.navigation.registration.bindGenericNavigation
import ru.vladislavsumin.qa.feature.debug.ui.screen.NavigationRegistrarImpl
import ru.vladislavsumin.qa.feature.debug.ui.screen.debug.DebugScreenFactory

fun Modules.featureDebug() = DI.Module("feature-debug") {
    bindGenericNavigation {
        val debugScreenFactory = DebugScreenFactory(i())
        NavigationRegistrarImpl(debugScreenFactory)
    }
}
