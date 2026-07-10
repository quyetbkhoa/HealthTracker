package com.quyetbkhoa.healthtracker.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.quyetbkhoa.healthtracker.core.designsystem.AppThemeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        val THEME_TYPE_KEY = stringPreferencesKey("theme_type")
        val LANGUAGE_KEY = stringPreferencesKey("language")
    }

    val themeTypeFlow: Flow<AppThemeType> = dataStore.data
        .map { preferences ->
            val themeString = preferences[THEME_TYPE_KEY] ?: AppThemeType.SYSTEM.name
            runCatching { AppThemeType.valueOf(themeString) }.getOrDefault(AppThemeType.SYSTEM)
        }

    val languageFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: "en" // Default is English
        }

    suspend fun saveThemeType(themeType: AppThemeType) {
        dataStore.edit { preferences ->
            preferences[THEME_TYPE_KEY] = themeType.name
        }
    }

    suspend fun saveLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }
}
