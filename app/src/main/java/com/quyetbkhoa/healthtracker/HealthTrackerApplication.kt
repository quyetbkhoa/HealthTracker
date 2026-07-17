package com.quyetbkhoa.healthtracker

import android.app.Application
import com.quyetbkhoa.healthtracker.data.seed.DemoDataInitializer
import com.quyetbkhoa.healthtracker.data.notification.ReminderNotificationManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HealthTrackerApplication : Application() {
    @Inject lateinit var demoDataInitializer: DemoDataInitializer
    @Inject lateinit var reminderNotificationManager: ReminderNotificationManager

    override fun onCreate() {
        super.onCreate()
        reminderNotificationManager.createChannel()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            demoDataInitializer.seedIfEmpty()
        }
    }
}
