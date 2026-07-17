package ru.vladislavsumin.qa.feature.settings.ui.screen.settings

import androidx.compose.runtime.Stable
import ru.vladislavsumin.core.decompose.components.ViewModel
import ru.vladislavsumin.core.factoryGenerator.GenerateFactory
import ru.vladislavsumin.qa.feature.settings.domain.AppLanguage
import ru.vladislavsumin.qa.feature.settings.domain.SettingsInteractorInternal

@GenerateFactory
@Stable
internal class SettingsScreenViewModel(private val settingsInteractor: SettingsInteractorInternal) : ViewModel() {
    val language = settingsInteractor.language
    val dumpPathOption = settingsInteractor.dumpPathOption

    fun onSelectLanguage(language: AppLanguage) = launch {
        settingsInteractor.setLanguage(language)
    }

    fun onSelectDumpPathTemp() = launch {
        settingsInteractor.setDumpPathTemp()
    }

    fun onSelectDumpPathCustom() = launch {
        settingsInteractor.setDumpPathCustom()
    }

    fun onDumpCustomPathChange(path: String) = launch {
        settingsInteractor.setDumpCustomPath(path)
    }
}
