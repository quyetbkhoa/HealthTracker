package com.quyetbkhoa.healthtracker.di

import com.quyetbkhoa.healthtracker.domain.repository.ReminderScheduler
import com.quyetbkhoa.healthtracker.platform.notification.AlarmReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    @Singleton
    abstract fun bindReminderScheduler(
        implementation: AlarmReminderScheduler
    ): ReminderScheduler
}
