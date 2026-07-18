package com.quyetbkhoa.healthtracker.widget

import android.annotation.SuppressLint
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
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
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
import com.quyetbkhoa.healthtracker.MainActivity
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.navigation.AppDestination
import com.quyetbkhoa.healthtracker.core.widget.HealthWidgetUpdater
import com.quyetbkhoa.healthtracker.domain.usecase.ObserveDashboardUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlin.math.abs

class QuickAddWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget()
}

class QuickAddWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val remainingCalories = loadRemainingCalories(context)
        provideContent {
            QuickAddWidgetContent(remainingCalories)
        }
    }

    private suspend fun loadRemainingCalories(context: Context): Int? = runCatching {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            QuickAddWidgetEntryPoint::class.java
        )
        val languageTag = context.resources.configuration.locales[0].language
            .takeIf { it == Locale.ENGLISH.language } ?: "vi"
        val dashboard = entryPoint.observeDashboardUseCase()(languageTag).first()
        dashboard.profile?.dailyCalorieTarget?.minus(dashboard.consumedCalories)
    }.getOrNull()
}

class QuickAddWidgetUpdater(
    private val context: Context
) : HealthWidgetUpdater {
    override suspend fun update() {
        QuickAddWidget().updateAll(context)
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface QuickAddWidgetEntryPoint {
    fun observeDashboardUseCase(): ObserveDashboardUseCase
}

@SuppressLint("RestrictedApi")
@Composable
private fun QuickAddWidgetContent(remainingCalories: Int?) {
    Row(
        modifier = GlanceModifier.fillMaxSize().padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuickAddButton(
                modifier = GlanceModifier.defaultWeight(),
                backgroundColor = Color(0xFF2E7D32),
                iconRes = R.drawable.ic_widget_meal,
                labelRes = R.string.widget_meal_short,
                destination = AppDestination.ADD_MEAL
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            QuickAddButton(
                modifier = GlanceModifier.defaultWeight(),
                backgroundColor = Color(0xFF1565C0),
                iconRes = R.drawable.ic_widget_activity,
                labelRes = R.string.widget_activity_short,
                destination = AppDestination.ADD_ACTIVITY
            )
        }
        Spacer(modifier = GlanceModifier.width(8.dp))
        RemainingCaloriesCard(
            modifier = GlanceModifier.defaultWeight(),
            remainingCalories = remainingCalories
        )
    }
}

@Composable
private fun QuickAddButton(
    modifier: GlanceModifier,
    backgroundColor: Color,
    iconRes: Int,
    labelRes: Int,
    destination: AppDestination
) {
    val context = LocalContext.current
    val label = context.getString(labelRes)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(backgroundColor)
            .cornerRadius(20.dp)
            .clickable(
                actionStartActivity<MainActivity>(
                    actionParametersOf(DestinationKey to destination.name)
                )
            )
            .padding(horizontal = 4.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = label,
                modifier = GlanceModifier.size(28.dp)
            )
            Spacer(modifier = GlanceModifier.size(7.dp))
            Text(
                text = label,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun RemainingCaloriesCard(
    modifier: GlanceModifier,
    remainingCalories: Int?
) {
    val context = LocalContext.current
    val isExceeded = remainingCalories != null && remainingCalories < 0
    val title = when {
        remainingCalories == null -> context.getString(R.string.widget_no_calorie_goal)
        isExceeded -> context.getString(R.string.widget_calories_exceeded)
        else -> context.getString(R.string.widget_calories_remaining)
    }
    val value = remainingCalories?.let {
        context.getString(R.string.widget_calorie_value, abs(it))
    } ?: "—"

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(if (isExceeded) Color(0xFFB3261E) else Color(0xFFF2A900))
            .cornerRadius(20.dp)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.size(5.dp))
            Text(
                text = title,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )
        }
    }
}

private val DestinationKey = ActionParameters.Key<String>(
    AppDestination.EXTRA_APP_DESTINATION
)
