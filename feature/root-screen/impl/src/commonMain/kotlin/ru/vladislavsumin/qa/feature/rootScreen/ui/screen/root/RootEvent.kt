package ru.vladislavsumin.qa.feature.rootScreen.ui.screen.root

sealed interface RootEvent {
    data class FocusTab(val number: Int) : RootEvent
}
