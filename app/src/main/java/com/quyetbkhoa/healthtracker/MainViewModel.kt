package com.quyetbkhoa.healthtracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.core.designsystem.AppThemeType
import com.quyetbkhoa.healthtracker.domain.repository.SettingsRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
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
    profileRepository: ProfileRepository
) : ViewModel() {

    val uiState: StateFlow<MainActivityUiState> = combine(
        settingsRepository.themeType,
        settingsRepository.language,
        profileRepository.userProfile
    ) { themeType, language, profile ->
        MainActivityUiState.Success(
            themeType = themeType,
            language = language,
            hasProfile = profile != null
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainActivityUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    fun setTheme(themeType: AppThemeType) {
        viewModelScope.launch {
            settingsRepository.setThemeType(themeType)
        }
    }
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(
        val themeType: AppThemeType,
        val language: String,
        val hasProfile: Boolean
    ) : MainActivityUiState
}
