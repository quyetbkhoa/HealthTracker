package com.quyetbkhoa.healthtracker.domain.usecase

import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import com.quyetbkhoa.healthtracker.domain.repository.ReminderScheduler
import com.quyetbkhoa.healthtracker.domain.repository.SettingsRepository
import javax.inject.Inject

class SetRemindersEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(isEnabled: Boolean, currentSettings: ReminderSettings) {
        settingsRepository.setRemindersEnabled(isEnabled)
        val updatedSettings = currentSettings.copy(isEnabled = isEnabled)
        if (isEnabled) reminderScheduler.scheduleAll(updatedSettings) else reminderScheduler.cancelAll()
    }
}

class SetReminderTimeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(
        type: ReminderType,
        time: ReminderTime,
        remindersEnabled: Boolean
    ) {
        settingsRepository.setReminderTime(type, time)
        if (remindersEnabled) reminderScheduler.scheduleNext(type, time)
    }
}
