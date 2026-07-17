package com.quyetbkhoa.healthtracker.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import com.quyetbkhoa.healthtracker.domain.repository.ReminderScheduler
import com.quyetbkhoa.healthtracker.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var reminderScheduler: ReminderScheduler
    @Inject lateinit var notificationManager: ReminderNotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmReminderScheduler.ACTION_REMINDER) return
        val type = ReminderType.fromName(
            intent.getStringExtra(AlarmReminderScheduler.EXTRA_REMINDER_TYPE)
        ) ?: return
        val isTestReminder = intent.getBooleanExtra(
            AlarmReminderScheduler.EXTRA_IS_TEST_REMINDER,
            false
        )
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val settings = settingsRepository.reminderSettings.first()
                if (settings.isEnabled) {
                    notificationManager.show(type, isTestReminder)
                    if (!isTestReminder) {
                        reminderScheduler.scheduleNext(type, settings.timeFor(type))
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
