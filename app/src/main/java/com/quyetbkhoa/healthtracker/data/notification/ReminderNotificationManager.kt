package com.quyetbkhoa.healthtracker.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.quyetbkhoa.healthtracker.MainActivity
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ReminderNotificationManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun show(type: ReminderType, isTestReminder: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_OPEN_REMINDER, type.name)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            type.notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val message = if (isTestReminder && type == ReminderType.DINNER) {
            R.string.notification_test_dinner_message
        } else when (type) {
            ReminderType.BREAKFAST -> R.string.notification_breakfast_message
            ReminderType.LUNCH -> R.string.notification_lunch_message
            ReminderType.DINNER -> R.string.notification_dinner_message
            ReminderType.ACTIVITY -> R.string.notification_activity_message
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_reminder)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(type.notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "health_reminders"
        const val EXTRA_OPEN_REMINDER = "open_reminder"
    }
}
