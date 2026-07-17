package ru.vladislavsumin.qa.feature.settings.domain

sealed interface DumpPathOption {
    data object Temp : DumpPathOption
    data class Custom(val path: String) : DumpPathOption
}
