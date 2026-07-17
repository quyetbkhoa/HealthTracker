package com.quyetbkhoa.healthtracker.data.repository

import com.quyetbkhoa.healthtracker.core.designsystem.AppThemeType
import com.quyetbkhoa.healthtracker.data.datastore.SettingsDataStore
import com.quyetbkhoa.healthtracker.domain.repository.SettingsRepository
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override val themeType: Flow<AppThemeType>
        get() = settingsDataStore.themeTypeFlow
    override val reminderSettings: Flow<ReminderSettings>
        get() = settingsDataStore.reminderSettingsFlow
    override val exactAlarmAccessRequested: Flow<Boolean>
        get() = settingsDataStore.exactAlarmAccessRequestedFlow

    override suspend fun setThemeType(themeType: AppThemeType) {
        settingsDataStore.saveThemeType(themeType)
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
