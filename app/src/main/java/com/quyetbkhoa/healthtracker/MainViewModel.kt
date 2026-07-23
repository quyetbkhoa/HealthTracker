package com.quyetbkhoa.healthtracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.domain.model.FontScale
import com.quyetbkhoa.healthtracker.domain.repository.SettingsRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import com.quyetbkhoa.healthtracker.domain.repository.ReminderScheduler
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import com.quyetbkhoa.healthtracker.domain.model.ThemeMode
import com.quyetbkhoa.healthtracker.domain.usecase.SetRemindersEnabledUseCase
import com.quyetbkhoa.healthtracker.domain.usecase.SetReminderTimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
    profileRepository: ProfileRepository,
    private val updateRemindersEnabled: SetRemindersEnabledUseCase,
    private val updateReminderTime: SetReminderTimeUseCase
) : ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = combine(
        settingsRepository.themeMode,
        settingsRepository.fontScale,
        profileRepository.userProfile,
        settingsRepository.reminderSettings,
        settingsRepository.exactAlarmAccessRequested
    ) { themeMode, fontScale, profile, reminderSettings, exactAlarmAccessRequested ->
        MainActivityUiState.Success(
            themeMode = themeMode,
            fontScale = fontScale,
            hasProfile = profile != null,
            reminderSettings = reminderSettings,
            exactAlarmAccessRequested = exactAlarmAccessRequested
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    fun setTheme(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
        }
    }

    fun setFontScale(fontScale: FontScale) {
        viewModelScope.launch {
            settingsRepository.setFontScale(fontScale)
        }
    }

    fun setRemindersEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            updateRemindersEnabled(isEnabled, currentReminderSettings())
        }
    }

    fun setReminderTime(type: ReminderType, time: ReminderTime) {
        viewModelScope.launch {
            updateReminderTime(type, time, currentReminderSettings().isEnabled)
        }
    }

    fun scheduleTestDinnerReminder() {
        reminderScheduler.scheduleTestDinnerReminder()
    }

    fun syncReminderSchedule() {
        val state = uiState.value as? MainActivityUiState.Success ?: return
        if (state.reminderSettings.isEnabled) {
            reminderScheduler.scheduleAll(state.reminderSettings)
        }
    }

    fun shouldRequestExactAlarmAccess(): Boolean {
        val state = uiState.value as? MainActivityUiState.Success ?: return false
        return state.reminderSettings.isEnabled && !state.exactAlarmAccessRequested
    }

    fun markExactAlarmAccessRequested() {
        viewModelScope.launch {
            settingsRepository.markExactAlarmAccessRequested()
        }
    }

    private fun currentReminderSettings(): ReminderSettings =
        (uiState.value as? MainActivityUiState.Success)?.reminderSettings ?: ReminderSettings()
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(
        val themeMode: ThemeMode,
        val fontScale: FontScale,
        val hasProfile: Boolean,
        val reminderSettings: ReminderSettings,
        val exactAlarmAccessRequested: Boolean
    ) : MainActivityUiState
}
