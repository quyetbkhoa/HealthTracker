package com.quyetbkhoa.healthtracker.domain.repository

import com.quyetbkhoa.healthtracker.core.designsystem.AppThemeType
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeType: Flow<AppThemeType>

    suspend fun setThemeType(themeType: AppThemeType)
}
