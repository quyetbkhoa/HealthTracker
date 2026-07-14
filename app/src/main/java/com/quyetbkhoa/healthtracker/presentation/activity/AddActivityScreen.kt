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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import kotlin.math.roundToInt

@Composable
fun AddActivityScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddActivityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val errorMessage = state.error?.let { addActivityErrorText(it) }

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
                .statusBarsPadding()
                .padding(horizontal = Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
        ) {
            AddActivityHeader(onNavigateBack)
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.isActivityPickerExpanded -> {
                    ActivityPickerHeader()
                    ActivityGrid(state.activities, state.selectedActivityId, onAction)
                }
                else -> {
                    state.selectedActivity?.let { SelectedActivityCard(it, onAction) }
                    DurationSection(state.durationMinutes, onAction)
                    CaloriesEstimate(state.displayedCalories)
                    Spacer(Modifier.weight(1f))
                    HealthPrimaryButton(
                        onClick = { onAction(AddActivityAction.SaveActivity) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.buttonHeightMedium),
                        enabled = state.canSave
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(Dimens.iconSizeMedium),
                                color = MaterialTheme.colorScheme.onPrimary
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
private fun AddActivityHeader(onNavigateBack: () -> Unit) {
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
                stringResource(R.string.add_activity_subtitle),
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.activityChoiceCardHeight)
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
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevationMedium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(activity.iconName, style = MaterialTheme.typography.headlineMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(Dimens.selectionIndicatorSize),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = stringResource(R.string.add_activity_selected),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
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
                            if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.primary
                        } else {
                            if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            Text(
                activity.name,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                stringResource(R.string.add_activity_met, activity.met),
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
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
    Card(
        shape = Shape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(activity.iconName, style = MaterialTheme.typography.headlineMedium)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.spaceSmall)
            ) {
                Text(
                    text = activity.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(stringResource(R.string.add_activity_met, activity.met))
            }
            OutlinedButton(
                onClick = { onAction(AddActivityAction.ReselectActivity) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
    Card(shape = Shape.large) {
        Column(
            modifier = Modifier.padding(Dimens.spaceMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.add_activity_duration),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        onAction(
                            AddActivityAction.ChangeDuration(
                                durationMinutes - AddActivityViewModel.DURATION_STEP_MINUTES
                            )
                        )
                    },
                    enabled = durationMinutes > AddActivityViewModel.SLIDER_MIN_DURATION_MINUTES
                ) {
                    Text(
                        text = stringResource(R.string.add_activity_minus_symbol),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Text(
                    stringResource(R.string.add_activity_minutes, durationMinutes),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        onAction(
                            AddActivityAction.ChangeDuration(
                                durationMinutes + AddActivityViewModel.DURATION_STEP_MINUTES
                            )
                        )
                    },
                    enabled = durationMinutes < AddActivityViewModel.SLIDER_MAX_DURATION_MINUTES
                ) {
                    Icon(Icons.Filled.Add, stringResource(R.string.add_activity_increase))
                }
            }
            Slider(
                value = durationMinutes.toFloat(),
                onValueChange = {
                    val stepped = (it / AddActivityViewModel.DURATION_STEP_MINUTES).roundToInt() *
                        AddActivityViewModel.DURATION_STEP_MINUTES
                    onAction(AddActivityAction.ChangeDuration(stepped))
                },
                valueRange = AddActivityViewModel.SLIDER_MIN_DURATION_MINUTES.toFloat()..
                    AddActivityViewModel.SLIDER_MAX_DURATION_MINUTES.toFloat(),
                steps = 34
            )
        }
    }
}

@Composable
private fun CaloriesEstimate(calories: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
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
                color = MaterialTheme.colorScheme.onSecondaryContainer
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
