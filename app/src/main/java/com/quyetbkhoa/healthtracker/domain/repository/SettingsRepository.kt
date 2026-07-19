package com.quyetbkhoa.healthtracker.domain.repository

import com.quyetbkhoa.healthtracker.core.designsystem.AppThemeType
import com.quyetbkhoa.healthtracker.core.designsystem.AppFontSize
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeType: Flow<AppThemeType>
    val fontSize: Flow<AppFontSize>
    val reminderSettings: Flow<ReminderSettings>
    val exactAlarmAccessRequested: Flow<Boolean>

    suspend fun setThemeType(themeType: AppThemeType)
    suspend fun setFontSize(fontSize: AppFontSize)
    suspend fun setRemindersEnabled(isEnabled: Boolean)
    suspend fun setReminderTime(type: ReminderType, time: ReminderTime)
    suspend fun markExactAlarmAccessRequested()
}
