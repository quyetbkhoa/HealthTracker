package com.quyetbkhoa.healthtracker

import android.app.Application
import android.util.Log
import com.quyetbkhoa.healthtracker.platform.notification.ReminderNotificationManager
import com.quyetbkhoa.healthtracker.core.widget.HealthWidgetUpdater
import com.quyetbkhoa.healthtracker.domain.usecase.ObserveRemainingCaloriesUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class HealthTrackerApplication : Application() {
    @Inject lateinit var reminderNotificationManager: ReminderNotificationManager
    @Inject lateinit var observeRemainingCalories: ObserveRemainingCaloriesUseCase
    @Inject lateinit var healthWidgetUpdater: HealthWidgetUpdater

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        reminderNotificationManager.createChannel()
        applicationScope.launch {
            val languageTag = resources.configuration.locales[0].language
                .takeIf { it == Locale.ENGLISH.language } ?: "vi"
            observeRemainingCalories(languageTag)
                .distinctUntilChanged()
                .collect { updateWidgetWithRetry() }
        }
    }

    private suspend fun updateWidgetWithRetry() {
        repeat(WIDGET_UPDATE_ATTEMPTS) { attempt ->
            try {
                healthWidgetUpdater.update()
                return
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val isLastAttempt = attempt == WIDGET_UPDATE_ATTEMPTS - 1
                if (isLastAttempt) {
                    Log.e(TAG, "Unable to refresh home screen widget", error)
                } else {
                    delay(WIDGET_UPDATE_RETRY_DELAY_MILLIS)
                }
            }
        }
    }

    private companion object {
        const val TAG = "HealthTrackerApp"
        const val WIDGET_UPDATE_ATTEMPTS = 3
        const val WIDGET_UPDATE_RETRY_DELAY_MILLIS = 500L
    }
}
