package com.quyetbkhoa.healthtracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import com.quyetbkhoa.healthtracker.MainActivity
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.data.notification.ReminderNotificationManager
import com.quyetbkhoa.healthtracker.domain.model.ReminderType

class QuickAddWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget()
}

class QuickAddWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            QuickAddWidgetContent()
        }
    }
}

@Composable
private fun QuickAddWidgetContent() {
    val context = LocalContext.current

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickAddButton(
            modifier = GlanceModifier.defaultWeight(),
            backgroundColor = Color(0xFF2E7D32),
            iconRes = R.drawable.ic_widget_meal,
            label = context.getString(R.string.dashboard_add_meal),
            destination = ReminderType.BREAKFAST
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        QuickAddButton(
            modifier = GlanceModifier.defaultWeight(),
            backgroundColor = Color(0xFF1565C0),
            iconRes = R.drawable.ic_widget_activity,
            label = context.getString(R.string.dashboard_add_activity),
            destination = ReminderType.ACTIVITY
        )
    }
}

@Composable
private fun QuickAddButton(
    modifier: GlanceModifier,
    backgroundColor: Color,
    iconRes: Int,
    label: String,
    destination: ReminderType
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(backgroundColor)
            .cornerRadius(24.dp)
            .clickable(
                actionStartActivity<MainActivity>(
                    actionParametersOf(DestinationKey to destination.name)
                )
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = label,
                modifier = GlanceModifier.size(36.dp)
            )
            Spacer(modifier = GlanceModifier.size(8.dp))
            Text(
                text = label,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )
        }
    }
}

private val DestinationKey = ActionParameters.Key<String>(
    ReminderNotificationManager.EXTRA_OPEN_REMINDER
)
