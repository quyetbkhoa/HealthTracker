package com.quyetbkhoa.healthtracker.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.quyetbkhoa.healthtracker.core.widget.HealthWidgetUpdater

class QuickAddWidgetUpdater(
    private val context: Context
) : HealthWidgetUpdater {
    override suspend fun update() {
        QuickAddWidget().updateAll(context)
    }
}
