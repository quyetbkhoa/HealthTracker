package com.quyetbkhoa.healthtracker.di

import com.quyetbkhoa.healthtracker.data.repository.ActivityRepositoryImpl
import com.quyetbkhoa.healthtracker.data.repository.DemoDataRepositoryImpl
import com.quyetbkhoa.healthtracker.data.repository.FoodRepositoryImpl
import com.quyetbkhoa.healthtracker.data.repository.MealRepositoryImpl
import com.quyetbkhoa.healthtracker.data.repository.ProfileRepositoryImpl
import com.quyetbkhoa.healthtracker.data.repository.SettingsRepositoryImpl
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
import com.quyetbkhoa.healthtracker.domain.repository.DemoDataRepository
import com.quyetbkhoa.healthtracker.domain.repository.FoodRepository
import com.quyetbkhoa.healthtracker.domain.repository.MealRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import com.quyetbkhoa.healthtracker.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindActivityRepository(
        implementation: ActivityRepositoryImpl
    ): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindDemoDataRepository(
        implementation: DemoDataRepositoryImpl
    ): DemoDataRepository

    @Binds
    @Singleton
    abstract fun bindFoodRepository(
        implementation: FoodRepositoryImpl
    ): FoodRepository

    @Binds
    @Singleton
    abstract fun bindMealRepository(
        implementation: MealRepositoryImpl
    ): MealRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        implementation: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        implementation: SettingsRepositoryImpl
    ): SettingsRepository
}
