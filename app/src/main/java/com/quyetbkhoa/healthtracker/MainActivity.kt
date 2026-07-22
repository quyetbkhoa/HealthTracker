package com.quyetbkhoa.healthtracker

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.os.LocaleListCompat
import androidx.core.net.toUri
import androidx.core.content.ContextCompat
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.navigation.AppNavigation
import com.quyetbkhoa.healthtracker.core.navigation.AppDestination
import com.quyetbkhoa.healthtracker.data.notification.ReminderNotificationManager
import com.quyetbkhoa.healthtracker.domain.model.AppLanguage
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val pendingDestination = MutableStateFlow<AppDestination?>(null)
    private val exactAlarmAccess = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updatePendingDestination(intent)
        updateExactAlarmAccess()
        ensureSupportedAppLanguage()
        enableEdgeToEdge()
        
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val destinationToOpen by pendingDestination.collectAsStateWithLifecycle()
            val hasExactAlarmAccess by exactAlarmAccess.collectAsStateWithLifecycle()
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                viewModel.setRemindersEnabled(isGranted)
                if (isGranted) requestExactAlarmAccessIfNeeded()
            }

            when (val state = uiState) {
                is MainActivityUiState.Loading -> {
                    // Show a simple loading screen while DataStore is being read
                    LoadingScreen()
                }
                is MainActivityUiState.Success -> {
                    LaunchedEffect(state.reminderSettings.isEnabled) {
                        if (state.reminderSettings.isEnabled) {
                            if (canPostNotifications()) {
                                viewModel.syncReminderSchedule()
                                requestExactAlarmAccessIfNeeded()
                            } else {
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                                )
                            }
                        }
                    }
                    HealthTrackerTheme(
                        themeType = state.themeType,
                        fontSize = state.fontSize
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            AppNavigation(
                                themeType = state.themeType,
                                fontSize = state.fontSize,
                                hasProfile = state.hasProfile,
                                reminderSettings = state.reminderSettings,
                                hasExactAlarmAccess = hasExactAlarmAccess,
                                destinationToOpen = destinationToOpen,
                                onThemeChanged = viewModel::setTheme,
                                onFontSizeChanged = viewModel::setFontSize,
                                onLanguageChanged = ::setAppLanguage,
                                onRemindersChanged = { isEnabled ->
                                    if (isEnabled && !canPostNotifications()) {
                                        notificationPermissionLauncher.launch(
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    } else {
                                        viewModel.setRemindersEnabled(isEnabled)
                                    }
                                },
                                onReminderTimeChanged = viewModel::setReminderTime,
                                onTestDinnerReminder = viewModel::scheduleTestDinnerReminder,
                                onRequestExactAlarmAccess = {
                                    requestExactAlarmAccessIfNeeded(force = true)
                                },
                                onDestinationConsumed = { pendingDestination.value = null }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        updatePendingDestination(intent)
    }

    override fun onResume() {
        super.onResume()
        updateExactAlarmAccess()
        viewModel.syncReminderSchedule()
    }

    private fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    private fun updatePendingDestination(intent: Intent?) {
        if (intent == null) return
        val widgetDestination = AppDestination.fromName(
            intent.getStringExtra(AppDestination.EXTRA_APP_DESTINATION)
        )
        val reminderDestination = ReminderType.fromName(
            intent.getStringExtra(ReminderNotificationManager.EXTRA_OPEN_REMINDER)
        ).toAppDestination()
        pendingDestination.value = widgetDestination ?: reminderDestination
        intent.removeExtra(AppDestination.EXTRA_APP_DESTINATION)
        intent.removeExtra(ReminderNotificationManager.EXTRA_OPEN_REMINDER)
    }

    private fun ReminderType?.toAppDestination(): AppDestination? = when (this) {
        ReminderType.BREAKFAST -> AppDestination.ADD_MEAL
        ReminderType.LUNCH -> AppDestination.ADD_LUNCH
        ReminderType.DINNER -> AppDestination.ADD_DINNER
        ReminderType.ACTIVITY -> AppDestination.ADD_ACTIVITY
        null -> null
    }

    private fun requestExactAlarmAccessIfNeeded(force: Boolean = false) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val alarmManager = getSystemService(AlarmManager::class.java)
        if (alarmManager.canScheduleExactAlarms() ||
            (!force && !viewModel.shouldRequestExactAlarmAccess())
        ) return

        viewModel.markExactAlarmAccessRequested()
        startActivity(
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = "package:$packageName".toUri()
            }
        )
    }

    private fun updateExactAlarmAccess() {
        exactAlarmAccess.value = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    }

    private fun ensureSupportedAppLanguage() {
        val languageTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (languageTag.isBlank()) {
            setAppLanguage(AppLanguage.VIETNAMESE)
        }
    }

    private fun setAppLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.languageTag)
        )
    }
}

@Composable
fun LoadingScreen() {
    HealthTrackerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Preview
@Composable
private fun PreviewLoadingScreen() {
    LoadingScreen()
}
