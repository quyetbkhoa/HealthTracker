package com.quyetbkhoa.healthtracker

import android.app.Application
import com.quyetbkhoa.healthtracker.data.seed.DemoDataInitializer
import com.quyetbkhoa.healthtracker.data.notification.ReminderNotificationManager
import com.quyetbkhoa.healthtracker.core.widget.HealthWidgetUpdater
import com.quyetbkhoa.healthtracker.domain.usecase.ObserveDashboardUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class HealthTrackerApplication : Application() {
    @Inject lateinit var demoDataInitializer: DemoDataInitializer
    @Inject lateinit var reminderNotificationManager: ReminderNotificationManager
    @Inject lateinit var observeDashboard: ObserveDashboardUseCase
    @Inject lateinit var healthWidgetUpdater: HealthWidgetUpdater

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        reminderNotificationManager.createChannel()
        applicationScope.launch {
            demoDataInitializer.seedIfEmpty()
        }
        applicationScope.launch {
            val languageTag = resources.configuration.locales[0].language
                .takeIf { it == Locale.ENGLISH.language } ?: "vi"
            observeDashboard(languageTag)
                .map { dashboard ->
                    dashboard.profile?.dailyCalorieTarget?.minus(dashboard.consumedCalories)
                }
                .distinctUntilChanged()
                .collect { healthWidgetUpdater.update() }
        }
    }
}
