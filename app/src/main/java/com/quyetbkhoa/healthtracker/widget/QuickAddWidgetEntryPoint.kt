package com.quyetbkhoa.healthtracker.widget

import com.quyetbkhoa.healthtracker.domain.usecase.ObserveRemainingCaloriesUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface QuickAddWidgetEntryPoint {
    fun observeRemainingCaloriesUseCase(): ObserveRemainingCaloriesUseCase
}
