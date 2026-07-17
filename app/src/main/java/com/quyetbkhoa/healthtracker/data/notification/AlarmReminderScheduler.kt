package com.quyetbkhoa.healthtracker.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.repository.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

class AlarmReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ReminderScheduler {
    private val alarmManager: AlarmManager
        get() = context.getSystemService(AlarmManager::class.java)

    override fun scheduleAll(settings: ReminderSettings) {
        ReminderType.entries.forEach { type ->
            scheduleNext(type, settings.timeFor(type))
        }
    }

    override fun scheduleNext(type: ReminderType, time: ReminderTime) {
        val now = System.currentTimeMillis()
        val nextTrigger = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        scheduleAlarm(
            nextTrigger,
            reminderPendingIntent(
                type,
                PendingIntent.FLAG_UPDATE_CURRENT
            ) ?: return
        )
    }

    override fun scheduleTestDinnerReminder() {
        val triggerAtMillis = System.currentTimeMillis() + TEST_DELAY_MILLIS
        scheduleAlarm(
            triggerAtMillis,
            testReminderPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        )
    }

    private fun scheduleAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    override fun cancelAll() {
        ReminderType.entries.forEach { type ->
            reminderPendingIntent(type, PendingIntent.FLAG_NO_CREATE)?.let(alarmManager::cancel)
            NotificationManagerCompat.from(context).cancel(type.notificationId)
        }
        testReminderPendingIntent(PendingIntent.FLAG_NO_CREATE)?.let(alarmManager::cancel)
    }

    private fun reminderPendingIntent(type: ReminderType, creationFlag: Int): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_REMINDER_TYPE, type.name)
        }
        return PendingIntent.getBroadcast(
            context,
            type.requestCode,
            intent,
            creationFlag or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun testReminderPendingIntent(creationFlag: Int): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_REMINDER_TYPE, ReminderType.DINNER.name)
            putExtra(EXTRA_IS_TEST_REMINDER, true)
        }
        return PendingIntent.getBroadcast(
            context,
            TEST_REMINDER_REQUEST_CODE,
            intent,
            creationFlag or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_REMINDER = "com.quyetbkhoa.healthtracker.action.REMINDER"
        const val EXTRA_REMINDER_TYPE = "reminder_type"
        const val EXTRA_IS_TEST_REMINDER = "is_test_reminder"
        private const val TEST_REMINDER_REQUEST_CODE = 9001
        private const val TEST_DELAY_MILLIS = 30_000L
    }
}
