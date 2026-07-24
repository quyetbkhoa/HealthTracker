package com.quyetbkhoa.healthtracker.presentation.mealjournal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthMarqueeText as Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.domain.model.FontScale
import com.quyetbkhoa.healthtracker.domain.model.ThemeMode
import com.quyetbkhoa.healthtracker.presentation.designsystem.Dimens
import com.quyetbkhoa.healthtracker.presentation.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.presentation.designsystem.MealTypeColorPalette
import com.quyetbkhoa.healthtracker.presentation.designsystem.Shape
import com.quyetbkhoa.healthtracker.presentation.designsystem.mealTypeColorPalette
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthIconText
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.healthColors
import com.quyetbkhoa.healthtracker.domain.model.MealEntry
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.usecase.DailyCalorieEvaluation
import com.quyetbkhoa.healthtracker.domain.usecase.DailyCalorieStatus
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
internal fun JournalHeader(onNavigateBack: () -> Unit, onPickDate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimens.spaceMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.navigate_back))
        }
        Text(
            text = stringResource(R.string.meal_journal_title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onPickDate) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(R.string.meal_journal_open_calendar),
                tint = MaterialTheme.healthColors.meal
            )
        }
    }
}

@Composable
internal fun DateNavigator(
    epochDay: Long,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPickDate: () -> Unit
) {
    val selectedDate = LocalDate.ofEpochDay(epochDay)
    val label = if (selectedDate == LocalDate.now()) {
        stringResource(
            R.string.meal_journal_today_date,
            selectedDate.format(
                DateTimeFormatter.ofPattern(stringResource(R.string.meal_journal_short_date_pattern))
            )
        )
    } else {
        selectedDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }

    HealthElevatedCard(modifier = Modifier.fillMaxWidth(), shape = Shape.large) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    stringResource(R.string.previous_day)
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Dimens.buttonHeightMedium)
                    .clickable(onClick = onPickDate),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.healthColors.meal)
                Text(
                    text = label,
                    modifier = Modifier.padding(start = Dimens.spaceSmall),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.healthColors.meal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onNext) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    stringResource(R.string.next_day)
                )
            }
        }
    }
}

