package com.quyetbkhoa.healthtracker.di

import com.quyetbkhoa.healthtracker.widget.HealthWidgetUpdater
import com.quyetbkhoa.healthtracker.widget.QuickAddWidgetUpdater
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {
    @Binds
    @Singleton
    abstract fun bindHealthWidgetUpdater(
        implementation: QuickAddWidgetUpdater
    ): HealthWidgetUpdater
}
