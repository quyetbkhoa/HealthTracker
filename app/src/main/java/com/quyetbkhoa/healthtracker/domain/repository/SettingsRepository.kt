package com.quyetbkhoa.healthtracker.domain.repository

import com.quyetbkhoa.healthtracker.domain.model.FontScale
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import com.quyetbkhoa.healthtracker.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val fontScale: Flow<FontScale>
    val reminderSettings: Flow<ReminderSettings>
    val exactAlarmAccessRequested: Flow<Boolean>

    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setFontScale(fontScale: FontScale)
    suspend fun setRemindersEnabled(isEnabled: Boolean)
    suspend fun setReminderTime(type: ReminderType, time: ReminderTime)
    suspend fun markExactAlarmAccessRequested()
}
