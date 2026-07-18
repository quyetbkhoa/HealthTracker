package com.quyetbkhoa.healthtracker.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.tooling.preview.Preview
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.core.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.domain.model.Gender
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun OnboardingSetupHeader(step: Int, onBack: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = Dimens.spaceExtraSmall, bottom = Dimens.spaceSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.spaceMedium), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(Dimens.buttonHeightMedium)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = onBack != null) { onBack?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.onboarding_setup_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.width(Dimens.buttonHeightMedium))
        }
        Text(
            text = stringResource(R.string.onboarding_setup_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        StepProgress(step = step)
    }
}

@Composable
private fun StepProgress(step: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        StepMarker(textRes = if (step == 1) R.string.onboarding_step_one else R.string.dashboard_icon_arrow, isActive = true)
        Box(modifier = Modifier.width(Dimens.buttonHeightLarge).height(Dimens.progressBarHeight).background(MaterialTheme.colorScheme.primary))
        StepMarker(textRes = R.string.onboarding_step_two, isActive = step == 2)
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(text = stringResource(R.string.onboarding_information), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(Dimens.spaceLarge))
        Text(text = stringResource(R.string.onboarding_target), style = MaterialTheme.typography.bodyLarge, color = if (step == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StepMarker(textRes: Int, isActive: Boolean) {
    Box(
        modifier = Modifier.size(OnboardingDimens.stepIndicatorSize).clip(CircleShape).background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(textRes), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupStep1Screen(
    uiState: ProfileSetupUiState,
    onAction: (ProfileSetupAction) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val datePickerState = rememberDatePickerState(selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis < System.currentTimeMillis()
    })
    val dobText = uiState.dateOfBirth?.takeIf { it > 0 }?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().format(DateTimeFormatter.ofPattern(stringResource(R.string.onboarding_date_display_pattern)))
    }.orEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { OnboardingSetupHeader(step = 1, onBack = onBack) }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(horizontal = Dimens.spaceMedium).padding(bottom = Dimens.spaceExtraLarge + Dimens.buttonHeightLarge),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceLarge)
            ) {
                HealthElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = Shape.extraLarge
                ) {
                    Column(modifier = Modifier.padding(Dimens.spaceLarge), verticalArrangement = Arrangement.spacedBy(Dimens.spaceLarge)) {
                        ProfileInputField(R.string.onboarding_full_name, uiState.fullName, R.string.onboarding_name_placeholder, uiState.fullNameError, KeyboardType.Text) {
                            onAction(ProfileSetupAction.UpdateFullName(it))
                        }
                        ProfileDateRow(dobText, uiState.dobError) {
                            focusManager.clearFocus()
                            showDatePicker = true
                        }
                        GenderSelector(selectedGender = uiState.gender) {
                            focusManager.clearFocus()
                            onAction(ProfileSetupAction.UpdateGender(it))
                        }
                        ProfileInputField(R.string.onboarding_height, uiState.heightStr, R.string.onboarding_height, uiState.heightError, KeyboardType.Number) {
                            onAction(ProfileSetupAction.UpdateHeight(it))
                        }
                        ProfileInputField(R.string.onboarding_weight, uiState.weightStr, R.string.onboarding_weight, uiState.weightError, KeyboardType.Number) {
                            onAction(ProfileSetupAction.UpdateWeight(it))
                        }
                    }
                }
            }
        }
        HealthPrimaryButton(
            onClick = { onAction(ProfileSetupAction.SubmitInformation) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceLarge)
                .height(Dimens.buttonHeightLarge)
                .shadow(Dimens.cardElevationLarge, Shape.medium)
        ) {
            Text(text = stringResource(R.string.onboarding_continue), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { onAction(ProfileSetupAction.UpdateDateOfBirth(it)) }; showDatePicker = false }) { Text(stringResource(R.string.onboarding_ok)) } },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.onboarding_cancel)) } }
            ) { DatePicker(state = datePickerState) }
        }
    }
}

@Composable
private fun GenderSelector(selectedGender: Gender, onGenderSelected: (Gender) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = stringResource(R.string.onboarding_gender), modifier = Modifier.width(OnboardingDimens.formLabelWidth), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(Dimens.spaceSmall))
        GenderOption(Gender.MALE, R.string.onboarding_gender_male, selectedGender == Gender.MALE, Modifier.weight(1f), onGenderSelected)
        Spacer(modifier = Modifier.width(Dimens.spaceSmall))
        GenderOption(Gender.FEMALE, R.string.onboarding_gender_female, selectedGender == Gender.FEMALE, Modifier.weight(1f), onGenderSelected)
    }
}

@Composable
private fun GenderOption(gender: Gender, labelRes: Int, isSelected: Boolean, modifier: Modifier, onGenderSelected: (Gender) -> Unit) {
    HealthElevatedCard(
        modifier = modifier.height(Dimens.buttonHeightMedium).clickable { onGenderSelected(gender) },
        shape = Shape.large,
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ProfileInputField(labelRes: Int, value: String, placeholderRes: Int, errorRes: Int?, keyboardType: KeyboardType, onValueChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = stringResource(labelRes), modifier = Modifier.width(OnboardingDimens.formLabelWidth), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(Dimens.spaceSmall))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(placeholderRes)) },
                isError = errorRes != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = if (keyboardType == KeyboardType.Number) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                shape = Shape.large,
                colors = onboardingFieldColors()
            )
            FieldError(errorRes)
        }
    }
}

@Composable
private fun ProfileDateRow(value: String, errorRes: Int?, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = stringResource(R.string.onboarding_dob), modifier = Modifier.width(OnboardingDimens.formLabelWidth), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(Dimens.spaceSmall))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeightLarge)
                    .clip(Shape.large)
                    .border(
                        width = Dimens.borderWidthThin,
                        color = if (errorRes != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                        shape = Shape.large
                    )
                    .clickable(onClick = onClick)
                    .padding(horizontal = Dimens.spaceMedium),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = value.ifBlank { stringResource(R.string.onboarding_dob_placeholder) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
            FieldError(errorRes)
        }
    }
}

@Composable
private fun FieldError(errorRes: Int?) {
    if (errorRes != null) Text(text = stringResource(errorRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun onboardingFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    errorBorderColor = MaterialTheme.colorScheme.error,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface
)

@Preview
@Composable
private fun PreviewProfileSetupStep1Screen() {
    HealthTrackerTheme {
        ProfileSetupStep1Screen(
            uiState = ProfileSetupUiState(fullName = "Nguyễn An", weightStr = "60", heightStr = "165"),
            onAction = {}
        )
    }
}

