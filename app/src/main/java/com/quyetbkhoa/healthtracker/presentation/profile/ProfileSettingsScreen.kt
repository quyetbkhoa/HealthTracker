package com.quyetbkhoa.healthtracker.presentation.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.domain.model.ActivityLevel
import com.quyetbkhoa.healthtracker.domain.model.Gender
import com.quyetbkhoa.healthtracker.domain.model.Goal
import com.quyetbkhoa.healthtracker.domain.usecase.BmiCategory
import com.quyetbkhoa.healthtracker.presentation.onboarding.SelectableCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ProfileSettingsScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ProfileSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.event.collect { if (it == ProfileSettingsEvent.Saved) onSaved() }
    }
    ProfileSettingsContent(state, viewModel::onAction, onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsContent(
    state: ProfileSettingsUiState,
    onAction: (ProfileSettingsAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.dateOfBirth,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis < System.currentTimeMillis()
        }
    )
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(Dimens.spaceLarge),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
        ) {
            Text(
                text = stringResource(R.string.profile_settings_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BmiCard(state)
            ProfileTextField(
                label = stringResource(R.string.onboarding_full_name),
                value = state.fullName,
                onValueChange = { onAction(ProfileSettingsAction.UpdateFullName(it)) },
                errorRes = state.fullNameError
            )
            val dobText = state.dateOfBirth?.takeIf { it > 0 }?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }.orEmpty()
            OutlinedTextField(
                value = dobText,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                enabled = false,
                label = { Text(stringResource(R.string.onboarding_dob)) },
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                isError = state.dobError != null,
                supportingText = state.dobError?.let { error -> { Text(stringResource(error)) } },
                shape = Shape.medium
            )
            FieldTitle(R.string.onboarding_gender)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                Gender.entries.forEach { gender ->
                    val label = when (gender) {
                        Gender.MALE -> R.string.onboarding_gender_male
                        Gender.FEMALE -> R.string.onboarding_gender_female
                        Gender.OTHER -> R.string.onboarding_gender_other
                    }
                    SelectableCardCompact(
                        text = stringResource(label),
                        selected = state.gender == gender,
                        onClick = { onAction(ProfileSettingsAction.UpdateGender(gender)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)) {
                ProfileTextField(
                    label = stringResource(R.string.onboarding_weight),
                    value = state.weightInput,
                    onValueChange = { onAction(ProfileSettingsAction.UpdateWeight(it)) },
                    errorRes = state.weightError,
                    suffix = stringResource(R.string.onboarding_weight_unit),
                    numeric = true,
                    modifier = Modifier.weight(1f)
                )
                ProfileTextField(
                    label = stringResource(R.string.onboarding_height),
                    value = state.heightInput,
                    onValueChange = { onAction(ProfileSettingsAction.UpdateHeight(it)) },
                    errorRes = state.heightError,
                    suffix = stringResource(R.string.onboarding_height_unit),
                    numeric = true,
                    modifier = Modifier.weight(1f)
                )
            }
            FieldTitle(R.string.onboarding_activity_level)
            ActivityLevel.entries.forEach { level ->
                val label = when (level) {
                    ActivityLevel.SEDENTARY -> R.string.onboarding_activity_sedentary
                    ActivityLevel.LIGHT -> R.string.onboarding_activity_light
                    ActivityLevel.MODERATE -> R.string.onboarding_activity_moderate
                    ActivityLevel.VERY_ACTIVE -> R.string.onboarding_activity_active
                    ActivityLevel.EXTRA_ACTIVE -> R.string.onboarding_activity_extra_active
                }
                SelectableCard(stringResource(label), state.activityLevel == level) {
                    onAction(ProfileSettingsAction.UpdateActivityLevel(level))
                }
            }
            FieldTitle(R.string.onboarding_goal)
            Goal.entries.forEach { goal ->
                val label = when (goal) {
                    Goal.LOSE_WEIGHT -> R.string.onboarding_goal_lose
                    Goal.MAINTAIN -> R.string.onboarding_goal_maintain
                    Goal.GAIN_WEIGHT -> R.string.onboarding_goal_gain
                }
                SelectableCard(stringResource(label), state.goal == goal) {
                    onAction(ProfileSettingsAction.UpdateGoal(goal))
                }
            }
            HealthPrimaryButton(
                onClick = { onAction(ProfileSettingsAction.Save) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeightMedium)
            ) { Text(stringResource(R.string.profile_settings_save)) }
            Spacer(modifier = Modifier.height(Dimens.spaceLarge))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onAction(ProfileSettingsAction.UpdateDateOfBirth(it)) }
                    showDatePicker = false
                }) { Text(stringResource(R.string.onboarding_ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.onboarding_cancel)) } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun BmiCard(state: ProfileSettingsUiState) {
    val category = when (state.bmiCategory) {
        BmiCategory.UNDERWEIGHT -> R.string.profile_bmi_underweight
        BmiCategory.NORMAL -> R.string.profile_bmi_normal
        BmiCategory.OVERWEIGHT -> R.string.profile_bmi_overweight
        BmiCategory.OBESE -> R.string.profile_bmi_obese
        null -> null
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = Shape.extraLarge
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Dimens.spaceLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(stringResource(R.string.profile_settings_bmi), color = MaterialTheme.colorScheme.onPrimaryContainer)
                category?.let { Text(stringResource(it), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) }
            }
            Text(
                text = state.bmi?.let { stringResource(R.string.profile_bmi_value, it) } ?: "—",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    errorRes: Int?,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    numeric: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text),
        isError = errorRes != null,
        supportingText = errorRes?.let { error -> { Text(stringResource(error)) } },
        singleLine = true,
        shape = Shape.medium
    )
}

@Composable
private fun FieldTitle(labelRes: Int) {
    Text(stringResource(labelRes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun SelectableCardCompact(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        ),
        shape = Shape.pill
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(Dimens.spaceMedium), contentAlignment = Alignment.Center) {
            Text(text, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
        }
    }
}
