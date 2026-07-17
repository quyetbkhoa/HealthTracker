package com.quyetbkhoa.healthtracker.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
class ReminderRescheduleReceiver : BroadcastReceiver() {
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val settings = settingsRepository.reminderSettings.first()
                if (settings.isEnabled) {
                    reminderScheduler.scheduleAll(settings)
                } else {
                    reminderScheduler.cancelAll()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
