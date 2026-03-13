package ru.vladislavsumin.feature.logViewer.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import ru.vladislavsumin.core.fs.FileSystemService

internal interface LogViewerSettingsRepository {
    val isStripDateEnabled: Flow<Boolean>
    suspend fun setIsStripDateEnabled(isEnabled: Boolean)
}

internal class LogViewerSettingsRepositoryImpl(
    private val fileSystemService: FileSystemService,
) : LogViewerSettingsRepository {
    private val isStripDateEnabledPreferenceKey = booleanPreferencesKey("is_strip_date_enabled")

    private val prefs = PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            fileSystemService.getPreferencesDir().toString().toPath().resolve("log_viewer_settings.preferences_pb")
        },
    )

    override val isStripDateEnabled: Flow<Boolean> = prefs.data.map { preferences ->
        preferences[isStripDateEnabledPreferenceKey] ?: false
    }

    override suspend fun setIsStripDateEnabled(isEnabled: Boolean) {
        prefs.edit { preferences -> preferences[isStripDateEnabledPreferenceKey] = isEnabled }
    }
}
