package ru.vladislavsumin.feature.logViewer.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import ru.vladislavsumin.core.fs.FileSystemService

internal interface LogViewerSettingsRepository {
    val isStripDateEnabled: Flow<Boolean>
    val logFontSize: Flow<Int>
    suspend fun setIsStripDateEnabled(isEnabled: Boolean)
    suspend fun setLogFontSize(size: Int)
}

internal class LogViewerSettingsRepositoryImpl(
    private val fileSystemService: FileSystemService,
) : LogViewerSettingsRepository {
    private val isStripDateEnabledPreferenceKey = booleanPreferencesKey("is_strip_date_enabled")
    private val logFontSizePreferenceKey = intPreferencesKey("log_font_size")

    private val prefs = PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            fileSystemService.getPreferencesDir().toString().toPath().resolve("log_viewer_settings.preferences_pb")
        },
    )

    override val isStripDateEnabled: Flow<Boolean> = prefs.data.map { preferences ->
        preferences[isStripDateEnabledPreferenceKey] ?: false
    }

    override val logFontSize: Flow<Int> = prefs.data.map { preferences ->
        preferences[logFontSizePreferenceKey] ?: DEFAULT_LOGS_FONT_SIZE
    }

    override suspend fun setIsStripDateEnabled(isEnabled: Boolean) {
        prefs.edit { preferences -> preferences[isStripDateEnabledPreferenceKey] = isEnabled }
    }

    override suspend fun setLogFontSize(size: Int) {
        prefs.edit { preferences -> preferences[logFontSizePreferenceKey] = size }
    }

    private companion object {
        private const val DEFAULT_LOGS_FONT_SIZE = 14 // body medium
    }
}
