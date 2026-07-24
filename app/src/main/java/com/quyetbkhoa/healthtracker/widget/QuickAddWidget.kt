package com.quyetbkhoa.healthtracker.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
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
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
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
import com.quyetbkhoa.healthtracker.navigation.AppDestination
import dagger.hilt.android.EntryPointAccessors
import java.util.Locale
import kotlinx.coroutines.flow.first
import kotlin.math.abs

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
        entryPoint.observeRemainingCaloriesUseCase()(languageTag).first()
    }.getOrNull()
}

@SuppressLint("RestrictedApi")
@Composable
private fun QuickAddWidgetContent(remainingCalories: Int?) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(R.color.widget_background)
            .cornerRadius(WIDGET_CORNER_RADIUS)
            .padding(WIDGET_PADDING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
        ) {
            QuickAddButton(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxWidth(),
                backgroundColorRes = R.color.widget_meal,
                iconRes = R.drawable.ic_widget_meal,
                labelRes = R.string.widget_meal_short,
                destination = AppDestination.ADD_MEAL
            )
            Spacer(modifier = GlanceModifier.height(ITEM_SPACING))
            QuickAddButton(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxWidth(),
                backgroundColorRes = R.color.widget_activity,
                iconRes = R.drawable.ic_widget_activity,
                labelRes = R.string.widget_activity_short,
                destination = AppDestination.ADD_ACTIVITY
            )
        }
        Spacer(modifier = GlanceModifier.width(ITEM_SPACING))
        RemainingCaloriesCard(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight(),
            remainingCalories = remainingCalories
        )
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun QuickAddButton(
    @ColorRes backgroundColorRes: Int,
    @DrawableRes iconRes: Int,
    @StringRes labelRes: Int,
    destination: AppDestination,
    modifier: GlanceModifier = GlanceModifier
) {
    val context = LocalContext.current
    val label = context.getString(labelRes)
    Box(
        modifier = modifier
            .background(backgroundColorRes)
            .cornerRadius(ITEM_CORNER_RADIUS)
            .clickable(
                actionStartActivity<MainActivity>(
                    actionParametersOf(DestinationKey to destination.name)
                )
            )
            .padding(horizontal = BUTTON_HORIZONTAL_PADDING),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = label,
                modifier = GlanceModifier.size(BUTTON_ICON_SIZE)
            )
            Spacer(modifier = GlanceModifier.width(BUTTON_CONTENT_SPACING))
            Text(
                text = label,
                style = TextStyle(
                    color = ColorProvider(R.color.widget_on_button),
                    fontSize = BUTTON_TEXT_SIZE,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun RemainingCaloriesCard(
    remainingCalories: Int?,
    modifier: GlanceModifier = GlanceModifier
) {
    val context = LocalContext.current
    val isExceeded = remainingCalories != null && remainingCalories < 0
    val labelRes = if (isExceeded) {
        R.string.widget_calories_exceeded
    } else {
        R.string.widget_calories_remaining
    }
    val backgroundColorRes = if (isExceeded) {
        R.color.widget_remaining_exceeded
    } else {
        R.color.widget_remaining_ok
    }
    val contentColorRes = if (isExceeded) {
        R.color.widget_on_remaining_exceeded
    } else {
        R.color.widget_on_remaining_ok
    }
    val value = remainingCalories?.let {
        context.getString(R.string.widget_calorie_value, abs(it))
    } ?: context.getString(R.string.widget_calorie_unavailable)

    Box(
        modifier = modifier
            .background(backgroundColorRes)
            .cornerRadius(ITEM_CORNER_RADIUS)
            .clickable(
                actionStartActivity<MainActivity>(
                    actionParametersOf(DestinationKey to AppDestination.DASHBOARD.name)
                )
            )
            .padding(REMAINING_CARD_PADDING),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = context.getString(labelRes),
                style = TextStyle(
                    color = ColorProvider(contentColorRes),
                    fontSize = REMAINING_LABEL_SIZE,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2
            )
            Spacer(modifier = GlanceModifier.height(REMAINING_CONTENT_SPACING))
            Text(
                text = value,
                style = TextStyle(
                    color = ColorProvider(contentColorRes),
                    fontSize = REMAINING_VALUE_SIZE,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
        }
    }
}

private val DestinationKey = ActionParameters.Key<String>(
    AppDestination.EXTRA_APP_DESTINATION
)

private val WIDGET_PADDING = 6.dp
private val ITEM_SPACING = 6.dp
private val WIDGET_CORNER_RADIUS = 22.dp
private val ITEM_CORNER_RADIUS = 16.dp
private val BUTTON_HORIZONTAL_PADDING = 8.dp
private val BUTTON_ICON_SIZE = 24.dp
private val BUTTON_CONTENT_SPACING = 8.dp
private val REMAINING_CARD_PADDING = 10.dp
private val REMAINING_CONTENT_SPACING = 6.dp
private val BUTTON_TEXT_SIZE = 12.sp
private val REMAINING_LABEL_SIZE = 13.sp
private val REMAINING_VALUE_SIZE = 26.sp
