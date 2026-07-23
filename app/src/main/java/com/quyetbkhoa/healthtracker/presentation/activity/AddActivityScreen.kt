package com.quyetbkhoa.healthtracker.presentation.activity

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthMarqueeText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.healthColors
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthIconText
import com.quyetbkhoa.healthtracker.core.designsystem.component.HealthNumericSlider
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.domain.model.OTHER_ACTIVITY_TYPE_ID
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun AddActivityScreen(
    epochDay: Long,
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddActivityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val errorMessage = state.error?.let { addActivityErrorText(it) }

    LaunchedEffect(epochDay) {
        viewModel.onAction(AddActivityAction.SetDate(epochDay))
    }
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { onSaved() }
    }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbar.showSnackbar(it) }
    }

    AddActivityContent(state, snackbar, viewModel::onAction, onNavigateBack)
}

@Composable
private fun AddActivityContent(
    state: AddActivityUiState,
    snackbar: SnackbarHostState,
    onAction: (AddActivityAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
        ) {
            AddActivityHeader(state.epochDay, onNavigateBack)
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.isActivityPickerExpanded -> {
                    ActivityPickerHeader()
                    ActivityGrid(state.activities, state.selectedActivityId, onAction)
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
                    ) {
                        state.selectedActivity?.let { SelectedActivityCard(it, onAction) }
                        DurationSection(state.durationMinutes, onAction)
                        CaloriesEstimate(state.displayedCalories)
                        HealthPrimaryButton(
                            onClick = { onAction(AddActivityAction.SaveActivity) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = Dimens.buttonHeightMedium),
                            enabled = state.canSave,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.healthColors.activity,
                                contentColor = MaterialTheme.healthColors.onActivity
                            )
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(Dimens.iconSizeMedium),
                                    color = MaterialTheme.healthColors.onActivity
                                )
                            } else {
                                Text(stringResource(R.string.add_activity_save))
                            }
                        }
                        Spacer(Modifier.height(Dimens.spaceMedium))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityPickerHeader() {
    var isInfoVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.add_activity_choose),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { isInfoVisible = true }) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = stringResource(R.string.add_activity_info)
            )
        }
    }

    if (isInfoVisible) {
        AlertDialog(
            onDismissRequest = { isInfoVisible = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null
                )
            },
            title = { Text(stringResource(R.string.add_activity_info_title)) },
            text = { Text(stringResource(R.string.add_activity_info_message)) },
            confirmButton = {
                TextButton(onClick = { isInfoVisible = false }) {
                    Text(stringResource(R.string.add_activity_info_confirm))
                }
            }
        )
    }
}

@Composable
private fun AddActivityHeader(epochDay: Long, onNavigateBack: () -> Unit) {
    val dateLabel = LocalDate.ofEpochDay(epochDay)
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.spaceMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.navigate_back))
        }
        Column {
            Text(
                stringResource(R.string.add_activity_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(R.string.add_activity_subtitle_date, dateLabel),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActivityGrid(
    activities: List<ActivityItemUiModel>,
    selectedId: Long?,
    onAction: (AddActivityAction) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
    ) {
        items(activities, key = { it.id }) { activity ->
            ActivityCard(activity, activity.id == selectedId, onAction)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActivityCard(
    activity: ActivityItemUiModel,
    isSelected: Boolean,
    onAction: (AddActivityAction) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    HealthElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.activityChoiceCardHeight)
            .combinedClickable(
                onClick = { onAction(AddActivityAction.SelectActivity(activity.id)) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAction(AddActivityAction.ToggleFavorite(activity.id))
                }
            ),
        shape = Shape.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.healthColors.activityContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (isSelected) {
                MaterialTheme.healthColors.onActivityContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.spaceMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                Dimens.spaceSmall,
                Alignment.CenterVertically
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HealthIconText(text = activity.iconName, style = MaterialTheme.typography.headlineMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(Dimens.selectionIndicatorSize),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = stringResource(R.string.add_activity_selected),
                                tint = MaterialTheme.healthColors.onActivityContainer
                            )
                        }
                    }
                    Icon(
                        imageVector = if (activity.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = stringResource(
                            if (activity.isFavorite) R.string.add_activity_favorite
                            else R.string.add_activity_not_favorite
                        ),
                        tint = if (activity.isFavorite) {
                            if (isSelected) MaterialTheme.healthColors.onActivityContainer
                            else MaterialTheme.healthColors.activity
                        } else {
                            if (isSelected) MaterialTheme.healthColors.onActivityContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            Text(
                activityDisplayName(activity),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.healthColors.onActivityContainer
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                stringResource(R.string.add_activity_met, activity.met),
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.healthColors.onActivityContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectedActivityCard(
    activity: ActivityItemUiModel,
    onAction: (AddActivityAction) -> Unit
) {
    HealthCard(
        shape = Shape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.healthColors.activityContainer,
            contentColor = MaterialTheme.healthColors.onActivityContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
            ) {
                HealthIconText(text = activity.iconName, style = MaterialTheme.typography.headlineMedium)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activityDisplayName(activity),
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(stringResource(R.string.add_activity_met, activity.met))
                }
            }
            OutlinedButton(
                onClick = { onAction(AddActivityAction.ReselectActivity) },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.healthColors.onActivityContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.add_activity_reselect),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DurationSection(
    durationMinutes: Int,
    onAction: (AddActivityAction) -> Unit
) {
    HealthNumericSlider(
        label = stringResource(R.string.add_activity_duration),
        value = durationMinutes.toString(),
        onValueChange = { input ->
            input.replace(',', '.').toFloatOrNull()?.let { value ->
                onAction(AddActivityAction.ChangeDuration(value.toInt()))
            }
        },
        valueRange = AddActivityViewModel.SLIDER_MIN_DURATION_MINUTES.toFloat()..
            AddActivityViewModel.SLIDER_MAX_DURATION_MINUTES.toFloat(),
        unit = stringResource(R.string.unit_minutes),
        step = AddActivityViewModel.DURATION_STEP_MINUTES.toFloat(),
        accentColor = MaterialTheme.healthColors.activity,
        accentContainerColor = MaterialTheme.healthColors.activityContainer,
        onAccentContainerColor = MaterialTheme.healthColors.onActivityContainer
    )
}

@Composable
private fun CaloriesEstimate(calories: Int) {
    HealthCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.healthColors.activityContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.add_activity_estimate))
            Text(
                stringResource(R.string.add_activity_kcal, calories),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.healthColors.onActivityContainer
            )
        }
    }
}

@Composable
private fun addActivityErrorText(error: AddActivityError): String = stringResource(
    when (error) {
        AddActivityError.NO_PROFILE_WEIGHT -> R.string.add_activity_error_weight
        AddActivityError.INVALID_DURATION -> R.string.add_activity_error_duration
        AddActivityError.INVALID_ACTIVITY -> R.string.add_activity_error_activity
        AddActivityError.SAVE_FAILED -> R.string.add_activity_error_save
    }
)

@Composable
private fun activityDisplayName(activity: ActivityItemUiModel): String =
    if (activity.id == OTHER_ACTIVITY_TYPE_ID) {
        stringResource(R.string.common_other)
    } else {
        activity.name
    }

@Preview
@Composable
private fun PreviewAddActivityScreen() {
    HealthTrackerTheme {
        AddActivityContent(
            state = AddActivityUiState(
                activities = listOf(ActivityItemUiModel(1L, "Đi bộ", 3.5, "walk", true)),
                selectedActivityId = 1L,
                durationMinutes = 30,
                weightKg = 60.0,
                estimatedCalories = 110.0,
                isActivityPickerExpanded = false,
                isLoading = false
            ),
            snackbar = SnackbarHostState(),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
