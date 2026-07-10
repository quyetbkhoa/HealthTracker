package com.quyetbkhoa.healthtracker.presentation.onboarding

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.domain.model.Gender
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun OnboardingTopBar(step: Int, onBack: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = Dimens.spaceMedium, bottom = Dimens.spaceMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(Dimens.topBarSpacing))
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall),
                    modifier = Modifier.padding(bottom = Dimens.spaceSmall)
                ) {
                    for (i in 1..3) {
                        Box(
                            modifier = Modifier
                                .height(Dimens.progressBarHeight)
                                .width(Dimens.progressBarWidth)
                                .background(
                                    color = if (i <= step) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                                    shape = Shape.extraSmall
                                )
                        )
                    }
                }
                Text(
                    text = stringResource(id = R.string.onboarding_step_format, step),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(Dimens.topBarSpacing))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupStep1Screen(
    uiState: ProfileSetupUiState,
    onAction: (ProfileSetupAction) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis < System.currentTimeMillis()
            }
        }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { OnboardingTopBar(step = 1, onBack = onBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimens.spaceLarge)
        ) {
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            Text(
                text = stringResource(id = R.string.onboarding_step1_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

            Text(
                text = stringResource(id = R.string.onboarding_step1_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.spaceExtraLarge))

            // Full Name
            Text(
                text = stringResource(id = R.string.onboarding_full_name),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))
            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = { onAction(ProfileSetupAction.UpdateFullName(it)) },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.onboarding_full_name_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = Shape.pill,
                isError = uiState.fullNameError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            if (uiState.fullNameError != null) {
                Text(
                    text = stringResource(id = uiState.fullNameError),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = Dimens.spaceMedium, top = Dimens.spaceExtraSmall)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spaceLarge))

            // Date of Birth
            Text(
                text = stringResource(id = R.string.onboarding_dob),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

            val dobText = if (uiState.dateOfBirth != null && uiState.dateOfBirth > 0) {
                val date = Instant.ofEpochMilli(uiState.dateOfBirth).atZone(ZoneId.systemDefault()).toLocalDate()
                date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } else {
                stringResource(id = R.string.onboarding_dob_placeholder)
            }

            OutlinedTextField(
                value = dobText,
                onValueChange = { },
                enabled = false,
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                shape = Shape.pill,
                isError = uiState.dobError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = if (uiState.dobError != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outline,
                    disabledTextColor = if (uiState.dateOfBirth != null && uiState.dateOfBirth > 0)
                        MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            if (uiState.dobError != null) {
                Text(
                    text = stringResource(id = uiState.dobError),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = Dimens.spaceMedium, top = Dimens.spaceExtraSmall)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spaceLarge))

            // Gender
            Text(
                text = stringResource(id = R.string.onboarding_gender),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Gender.entries.forEach { gender ->
                    val isSelected = uiState.gender == gender
                    val textRes = when (gender) {
                        Gender.MALE -> R.string.onboarding_gender_male
                        Gender.FEMALE -> R.string.onboarding_gender_female
                        Gender.OTHER -> R.string.onboarding_gender_other
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimens.genderBoxHeight)
                            .padding(horizontal = Dimens.spaceSmall)
                            .shadow(
                                elevation = if (!isSelected) Dimens.spaceExtraSmall else Dimens.borderWidthThin,
                                shape = Shape.pill,
                                spotColor = MaterialTheme.colorScheme.outline
                            )
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface,
                                shape = Shape.pill
                            )
                            .clickable { onAction(ProfileSetupAction.UpdateGender(gender)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = textRes),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            HealthPrimaryButton(
                onClick = { onAction(ProfileSetupAction.SubmitStep1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeightLarge)
                    .padding(bottom = Dimens.spaceLarge)
            ) {
                Text(
                    text = stringResource(id = R.string.onboarding_continue),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onAction(ProfileSetupAction.UpdateDateOfBirth(it))
                        }
                        showDatePicker = false
                    }) { Text(stringResource(id = R.string.onboarding_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(id = R.string.onboarding_cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
