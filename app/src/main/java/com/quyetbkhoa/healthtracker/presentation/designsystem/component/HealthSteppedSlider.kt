package com.quyetbkhoa.healthtracker.presentation.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthMarqueeText as Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.quyetbkhoa.healthtracker.presentation.designsystem.Dimens
import com.quyetbkhoa.healthtracker.presentation.designsystem.Shape
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthCard
import kotlin.math.roundToInt

/**
 * A discrete selector that keeps every available value on an explicit slider stop.
 */
@Composable
fun HealthSteppedSlider(
    label: String,
    options: List<String>,
    selectedOptionLabels: List<String> = options,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    accentContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onAccentContainerColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    if (options.isEmpty()) return

    val safeSelectedIndex = selectedIndex.coerceIn(options.indices)
    val lastIndex = options.lastIndex

    HealthCard(
        modifier = modifier.fillMaxWidth(),
        shape = Shape.large
    ) {
        Column(
            modifier = Modifier.padding(Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceExtraSmall)
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
                    shape = Shape.pill,
                    colors = CardDefaults.cardColors(
                        containerColor = accentContainerColor,
                        contentColor = onAccentContainerColor
                    )
                ) {
                    Text(
                        text = selectedOptionLabels.getOrElse(safeSelectedIndex) {
                            options[safeSelectedIndex]
                        },
                        modifier = Modifier.padding(
                            horizontal = Dimens.spaceMedium,
                            vertical = Dimens.spaceSmall
                        ),
                        fontWeight = FontWeight.Bold,
                        color = onAccentContainerColor,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Slider(
                value = safeSelectedIndex.toFloat(),
                onValueChange = { value ->
                    onSelectedIndexChange(value.roundToInt().coerceIn(options.indices))
                },
                valueRange = 0f..lastIndex.toFloat().coerceAtLeast(1f),
                steps = (options.size - 2).coerceAtLeast(0),
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    activeTickColor = onAccentContainerColor,
                    inactiveTrackColor = accentContainerColor,
                    inactiveTickColor = accentColor
                )
            )

            val labelIndices = listOf(0, lastIndex / 2, lastIndex).distinct()
            Row(modifier = Modifier.fillMaxWidth()) {
                labelIndices.forEachIndexed { position, optionIndex ->
                    Text(
                        text = options[optionIndex],
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (optionIndex == safeSelectedIndex) {
                            accentColor
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (optionIndex == safeSelectedIndex) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },
                        textAlign = when (position) {
                            0 -> TextAlign.Start
                            labelIndices.lastIndex -> TextAlign.End
                            else -> TextAlign.Center
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
