package com.quyetbkhoa.healthtracker.core.designsystem.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.Shape as HealthShape

@Composable
fun HealthCard(
    modifier: Modifier = Modifier,
    shape: Shape = HealthShape.large,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

@Composable
fun HealthOutlinedCard(
    modifier: Modifier = Modifier,
    shape: Shape = HealthShape.large,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.background
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = Dimens.borderWidthThin,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        content = content
    )
}

@Composable
fun HealthElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = HealthShape.large,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.padding(Dimens.borderWidthThin),
        shape = shape,
        colors = colors,
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.cardElevationSmall
        ),
        content = content
    )
}
