package com.quyetbkhoa.healthtracker.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.quyetbkhoa.healthtracker.widget.HealthWidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class QuickAddWidgetUpdater @Inject constructor(
    @param:ApplicationContext private val context: Context
) : HealthWidgetUpdater {
    override suspend fun update() {
        QuickAddWidget().updateAll(context)
    }
}
