package com.quyetbkhoa.healthtracker.data.repository

import com.quyetbkhoa.healthtracker.data.datastore.SettingsDataStore
import com.quyetbkhoa.healthtracker.domain.model.FontScale
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import com.quyetbkhoa.healthtracker.domain.model.ThemeMode
import com.quyetbkhoa.healthtracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override val themeMode: Flow<ThemeMode>
        get() = settingsDataStore.themeModeFlow
    override val fontScale: Flow<FontScale>
        get() = settingsDataStore.fontScaleFlow
    override val reminderSettings: Flow<ReminderSettings>
        get() = settingsDataStore.reminderSettingsFlow
    override val exactAlarmAccessRequested: Flow<Boolean>
        get() = settingsDataStore.exactAlarmAccessRequestedFlow

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        settingsDataStore.saveThemeMode(themeMode)
    }

    override suspend fun setFontScale(fontScale: FontScale) {
        settingsDataStore.saveFontScale(fontScale)
    }

    override suspend fun setRemindersEnabled(isEnabled: Boolean) {
        settingsDataStore.saveRemindersEnabled(isEnabled)
    }

    override suspend fun setReminderTime(type: ReminderType, time: ReminderTime) {
        settingsDataStore.saveReminderTime(type, time)
    }

    override suspend fun markExactAlarmAccessRequested() {
        settingsDataStore.markExactAlarmAccessRequested()
    }

}
