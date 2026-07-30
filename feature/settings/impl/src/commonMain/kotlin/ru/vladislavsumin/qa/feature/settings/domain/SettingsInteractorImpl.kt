package ru.vladislavsumin.qa.feature.settings.domain

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import ru.vladislavsumin.core.coroutines.dispatcher.VsDispatchers
import ru.vladislavsumin.core.fs.FileSystemService

private val DUMP_PATH_DEFAULT_DIR =
    System.getProperty("user.home") + System.getProperty("file.separator") + "Downloads"

internal interface SettingsInteractorInternal : SettingsInteractor {
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setDumpPathMode(mode: DumpPathMode)
    suspend fun setDumpCustomPath(path: String)
    suspend fun setDumpPathTemp()
    suspend fun setDumpPathCustom()
    suspend fun setLiquidGlass(enabled: Boolean)
}

internal enum class DumpPathMode { TEMP, CUSTOM }

// TODO Нужно вынести работу с префами с core.
internal class SettingsInteractorImpl(fileSystemService: FileSystemService, private val dispatchers: VsDispatchers) :
    SettingsInteractorInternal {
    private val languagePreferenceKey = stringPreferencesKey("app_language")
    private val dumpPathModeKey = stringPreferencesKey("dump_path_mode")
    private val dumpCustomPathKey = stringPreferencesKey("dump_custom_path")
    private val liquidGlassKey = booleanPreferencesKey("liquid_glass")

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

    override val dumpPathOption: Flow<DumpPathOption> = prefs.data
        .map { preferences ->
            val mode = preferences[dumpPathModeKey]
                ?.let { runCatching { DumpPathMode.valueOf(it) }.getOrNull() }
                ?: DumpPathMode.TEMP
            val customPath = preferences[dumpCustomPathKey] ?: DUMP_PATH_DEFAULT_DIR
            when (mode) {
                DumpPathMode.TEMP -> DumpPathOption.Temp
                DumpPathMode.CUSTOM -> DumpPathOption.Custom(customPath)
            }
        }

    override val isLiquidGlass: Flow<Boolean> = prefs.data
        .map { preferences -> preferences[liquidGlassKey] ?: true }

    override suspend fun setLanguage(language: AppLanguage): Unit = withContext(dispatchers.IO) {
        prefs.edit { preferences -> preferences[languagePreferenceKey] = language.name }
    }

    override suspend fun setDumpPathMode(mode: DumpPathMode): Unit = withContext(dispatchers.IO) {
        prefs.edit { preferences -> preferences[dumpPathModeKey] = mode.name }
    }

    override suspend fun setDumpCustomPath(path: String): Unit = withContext(dispatchers.IO) {
        prefs.edit { preferences -> preferences[dumpCustomPathKey] = path }
    }

    override suspend fun setDumpPathTemp(): Unit = setDumpPathMode(DumpPathMode.TEMP)

    override suspend fun setDumpPathCustom(): Unit = setDumpPathMode(DumpPathMode.CUSTOM)

    override suspend fun setLiquidGlass(enabled: Boolean): Unit = withContext(dispatchers.IO) {
        prefs.edit { preferences -> preferences[liquidGlassKey] = enabled }
    }
}
