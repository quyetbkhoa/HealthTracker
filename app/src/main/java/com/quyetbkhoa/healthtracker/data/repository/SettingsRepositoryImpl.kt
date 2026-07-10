package com.quyetbkhoa.healthtracker.data.repository

import com.quyetbkhoa.healthtracker.core.designsystem.AppThemeType
import com.quyetbkhoa.healthtracker.data.datastore.SettingsDataStore
import com.quyetbkhoa.healthtracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override val themeType: Flow<AppThemeType>
        get() = settingsDataStore.themeTypeFlow

    override val language: Flow<String>
        get() = settingsDataStore.languageFlow

    override suspend fun setThemeType(themeType: AppThemeType) {
        settingsDataStore.saveThemeType(themeType)
    }

    override suspend fun setLanguage(language: String) {
        settingsDataStore.saveLanguage(language)
    }
}
