package ru.vladislavsumin.feature.logViewer.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import ru.vladislavsumin.core.fs.FileSystemService
import ru.vladislavsumin.feature.logViewer.LogLogger

internal interface SavedFiltersRepository {
    fun observeSavedFilters(): Flow<List<SavedFilter>>
    suspend fun add(name: String, content: String)
    suspend fun update(oldFilter: SavedFilter, newName: String, newContent: String)
    suspend fun remove(filter: SavedFilter)

    @Serializable
    data class SavedFilter(val name: String, val content: String)
}

// TODO перевести это все на нормальные префы
internal class SavedFiltersRepositoryImpl(private val fileSystemService: FileSystemService) : SavedFiltersRepository {
    private val savedFiltersPreferenceKey = stringPreferencesKey("saved_filters")

    private val prefs = PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            fileSystemService.getPreferencesDir().toString().toPath().resolve("saved_filters.preferences_pb")
        },
    )

    override fun observeSavedFilters(): Flow<List<SavedFiltersRepository.SavedFilter>> = prefs.data.map { preferences ->
        preferences[savedFiltersPreferenceKey]?.let {
            Json.decodeFromString<List<SavedFiltersRepository.SavedFilter>>(it)
        } ?: emptyList()
    }

    override suspend fun add(name: String, content: String) {
        prefs.edit { preferences ->
            val oldList = observeSavedFilters().first()

            if (oldList.firstOrNull { it.name == name } != null) {
                LogLogger.e { "Filter with name $name already exists" }
                return@edit
            }

            preferences[savedFiltersPreferenceKey] = Json.encodeToString(
                oldList + SavedFiltersRepository.SavedFilter(name, content),
            )
        }
    }

    override suspend fun update(oldFilter: SavedFiltersRepository.SavedFilter, newName: String, newContent: String) {
        prefs.edit { preferences ->
            val oldList = observeSavedFilters().first()

            val oldIndex = oldList.indexOf(oldFilter)
            if (oldIndex == -1) {
                LogLogger.e { "Filter ${oldFilter.name} not found" }
                return@edit
            }

            if (newName != oldFilter.name && oldList.any { it.name == newName }) {
                LogLogger.e { "Filter with name $newName already exists" }
                return@edit
            }

            val newList = oldList.toMutableList()
            newList[oldIndex] = SavedFiltersRepository.SavedFilter(newName, newContent)
            preferences[savedFiltersPreferenceKey] = Json.encodeToString(newList)
        }
    }

    override suspend fun remove(filter: SavedFiltersRepository.SavedFilter) {
        val newData = observeSavedFilters().first().toMutableList()
        newData.remove(filter)
        prefs.edit { preferences ->
            preferences[savedFiltersPreferenceKey] = Json.encodeToString(newData)
        }
    }
}
