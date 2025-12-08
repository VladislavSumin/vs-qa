package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

import androidx.compose.ui.input.key.Key
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.channels.Channel
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.core.navigation.IntentScreenParams
import ru.vladislavsumin.core.navigation.viewModel.NavigationViewModel
import ru.vladislavsumin.core.ui.hotkeyController.GlobalHotkeyManager
import ru.vladislavsumin.core.ui.hotkeyController.KeyModifier
import ru.vladislavsumin.qa.feature.homeScreen.ui.screen.home.HomeScreenParams

@GenerateFactory
internal class RootViewModel(
    globalHotkeyManager: GlobalHotkeyManager,
) : NavigationViewModel() {
    val events = Channel<RootEvent>()

    init {
        relaunchOnUiLifecycle(Lifecycle.State.RESUMED) {
            @Suppress("MagicNumber")
            globalHotkeyManager.subscribe(
                KeyModifier.Command + Key.N to { focusTab(0) },
                KeyModifier.Command + Key.One to { focusTab(1) },
                KeyModifier.Command + Key.Two to { focusTab(2) },
                KeyModifier.Command + Key.Three to { focusTab(3) },
                KeyModifier.Command + Key.Four to { focusTab(4) },
                KeyModifier.Command + Key.Five to { focusTab(5) },
                KeyModifier.Command + Key.Six to { focusTab(6) },
                KeyModifier.Command + Key.Seven to { focusTab(7) },
                KeyModifier.Command + Key.Eight to { focusTab(8) },
                KeyModifier.Command + Key.Nine to { focusTab(9) },
                KeyModifier.Command + Key.Zero to { focusTab(10) },
            )
        }
    }

    private fun focusTab(number: Int): Boolean {
        events.trySend(RootEvent.FocusTab(number))
        return true
    }

    fun onTabClick(tabScreenParams: IntentScreenParams<*>) = open(tabScreenParams)
    fun onCloseTabClick(tabScreenParams: IntentScreenParams<*>) = close(tabScreenParams)
    fun onClickHome() = open(HomeScreenParams)
}
