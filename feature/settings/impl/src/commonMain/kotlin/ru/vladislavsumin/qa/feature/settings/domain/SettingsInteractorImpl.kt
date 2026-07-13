package ru.vladislavsumin.qa.feature.settings.domain

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers
import ru.vladislavsumin.core.fs.FileSystemService

internal interface SettingsInteractorInternal : SettingsInteractor {
    suspend fun setLanguage(language: AppLanguage)
}

// TODO Нужно вынести работу с префами с core.
internal class SettingsInteractorImpl(fileSystemService: FileSystemService, private val dispatchers: VsDispatchers) :
    SettingsInteractorInternal {
    private val languagePreferenceKey = stringPreferencesKey("app_language")

    private val prefs = PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            fileSystemService.getPreferencesDir().toString().toPath().resolve("settings.preferences_pb")
        },
    )

    override val language: Flow<AppLanguage> = prefs.data
        .map { preferences ->
            preferences[languagePreferenceKey]
                ?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
                ?: AppLanguage.SYSTEM
        }

    override suspend fun setLanguage(language: AppLanguage): Unit = withContext(dispatchers.IO) {
        prefs.edit { preferences -> preferences[languagePreferenceKey] = language.name }
    }
}
