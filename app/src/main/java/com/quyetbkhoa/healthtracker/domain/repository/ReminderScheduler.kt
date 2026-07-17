package com.quyetbkhoa.healthtracker.domain.repository

import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime

interface ReminderScheduler {
    fun scheduleAll(settings: ReminderSettings)
    fun scheduleNext(type: ReminderType, time: ReminderTime)
    fun scheduleTestDinnerReminder()
    fun cancelAll()
}
