package com.quyetbkhoa.healthtracker.presentation.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthMarqueeText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.presentation.designsystem.Dimens
import com.quyetbkhoa.healthtracker.presentation.designsystem.Shape
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthCard
import kotlin.math.roundToInt

/** Slider-first numeric input. The value chip remains keyboard-accessible for precision. */
@Composable
fun HealthNumericSlider(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    unit: String = "",
    step: Float = 1f,
    errorText: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    accentContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onAccentContainerColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    var isEditorVisible by remember { mutableStateOf(false) }
    var editorValue by remember(value, isEditorVisible) { mutableStateOf(value) }
    val numericValue = value.toFloatOrNull()
    val sliderValue = (numericValue ?: valueRange.start).coerceIn(valueRange)
    val totalSteps = ((valueRange.endInclusive - valueRange.start) / step).roundToInt()
    val stepCount = if (totalSteps in 2..MAX_VISIBLE_SLIDER_DIVISIONS) totalSteps - 1 else 0

    HealthCard(modifier = modifier.fillMaxWidth(), shape = Shape.large) {
        Column(
            modifier = Modifier.padding(Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                HealthCard(
                    modifier = Modifier
                        .heightIn(min = Dimens.buttonHeightMedium)
                        .clickable {
                            editorValue = value
                            isEditorVisible = true
                        },
                    shape = Shape.pill,
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = accentContainerColor,
                        contentColor = onAccentContainerColor
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .heightIn(min = Dimens.buttonHeightMedium)
                            .padding(horizontal = Dimens.spaceMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(
                                R.string.numeric_value_with_unit,
                                value.ifBlank { stringResource(R.string.common_not_available) },
                                unit
                            ).trim(),
                            fontWeight = FontWeight.Bold,
                            color = onAccentContainerColor,
                            maxLines = 1
                        )
                    }
                }
            }
            Slider(
                value = sliderValue,
                onValueChange = { rawValue ->
                    val stepped = ((rawValue - valueRange.start) / step).roundToInt() * step +
                        valueRange.start
                    onValueChange(formatSliderValue(stepped.coerceIn(valueRange), step))
                },
                valueRange = valueRange,
                steps = stepCount,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    activeTickColor = onAccentContainerColor,
                    inactiveTrackColor = accentContainerColor,
                    inactiveTickColor = accentColor
                )
            )
            if (errorText != null) {
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (isEditorVisible) {
        AlertDialog(
            onDismissRequest = { isEditorVisible = false },
            title = { Text(stringResource(R.string.numeric_input_title, label)) },
            text = {
                OutlinedTextField(
                    value = editorValue,
                    onValueChange = { editorValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    suffix = unit.takeIf(String::isNotBlank)?.let { suffix ->
                        { Text(suffix) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = Shape.medium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValueChange(editorValue)
                        isEditorVisible = false
                    },
                    enabled = editorValue.replace(',', '.').toFloatOrNull()?.let {
                        it.isFinite() && it > 0f
                    } == true
                ) {
                    Text(stringResource(R.string.common_apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditorVisible = false }) {
                    Text(stringResource(R.string.onboarding_cancel))
                }
            }
        )
    }
}

private fun formatSliderValue(value: Float, step: Float): String =
    if (step >= 1f) value.roundToInt().toString()
    else (value * 10f).roundToInt().div(10f).toString()

private const val MAX_VISIBLE_SLIDER_DIVISIONS = 12
