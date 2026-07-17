package com.quyetbkhoa.healthtracker

import android.app.Application
import com.quyetbkhoa.healthtracker.data.seed.DemoDataInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HealthTrackerApplication : Application() {
    @Inject lateinit var demoDataInitializer: DemoDataInitializer

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            demoDataInitializer.seedIfEmpty()
        }
    }
}
