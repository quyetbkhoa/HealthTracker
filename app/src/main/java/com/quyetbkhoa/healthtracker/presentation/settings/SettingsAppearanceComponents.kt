package com.quyetbkhoa.healthtracker.presentation.settings

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthMarqueeText as Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.presentation.designsystem.Dimens
import com.quyetbkhoa.healthtracker.presentation.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.presentation.designsystem.Shape
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthIconText
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthElevatedCard
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.card.HealthOutlinedCard
import com.quyetbkhoa.healthtracker.domain.model.AppLanguage
import com.quyetbkhoa.healthtracker.domain.model.FontScale
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import com.quyetbkhoa.healthtracker.domain.model.ThemeMode

@Composable
internal fun SettingsHeader(onNavigateBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = Dimens.spaceSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.navigate_back)
                )
            }
            Text(
                text = stringResource(R.string.screen_settings),
                modifier = Modifier.padding(start = Dimens.spaceSmall),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
internal fun SettingsSectionCard(
    icon: String,
    title: String,
    content: @Composable ColumnScope.() -> Unit
){
    HealthElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.large
    ){
        Column(
            modifier = Modifier.padding(Dimens.spaceMedium)
        ){
            //title
            Row(verticalAlignment = Alignment.CenterVertically) {
                HealthIconText(text = icon, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = title,
                    modifier = Modifier.padding(start = Dimens.spaceSmall),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            //content
            content()


        }
    }
}

@Composable
internal fun SettingsNavigationCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    HealthOutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = Shape.large
    ) {
        Row(
            modifier = Modifier.padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIconBubble(icon = icon)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimens.spaceMedium)
            ) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HealthIconText(
                text = stringResource(R.string.settings_icon_chevron),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
internal fun ThemeChoice(
    type: ThemeMode,
    selected: Boolean,
    modifier: Modifier,
    onClick: (ThemeMode) -> Unit
) {
    val label = stringResource(
        when (type) {
            ThemeMode.LIGHT -> R.string.theme_light
            ThemeMode.DARK -> R.string.theme_dark
            ThemeMode.PINK -> R.string.theme_pink
            ThemeMode.SYSTEM -> R.string.theme_system
        }
    )
    val icon = stringResource(
        when (type) {
            ThemeMode.LIGHT -> R.string.settings_icon_light
            ThemeMode.DARK -> R.string.settings_icon_dark
            ThemeMode.PINK -> R.string.settings_icon_pink
            ThemeMode.SYSTEM -> R.string.settings_icon_system
        }
    )
    HealthCard(
        modifier = modifier.clickable { onClick(type) },
        shape = Shape.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = Dimens.buttonHeightLarge)
                .padding(Dimens.spaceMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            HealthIconText(
                text = icon,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun FontSizeChoice(
    scale: FontScale,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (FontScale) -> Unit
) {
    val label = stringResource(
        when (scale) {
            FontScale.SMALL -> R.string.settings_font_size_small
            FontScale.MEDIUM -> R.string.settings_font_size_medium
            FontScale.LARGE -> R.string.settings_font_size_large
        }
    )
    val sampleStyle = when (scale) {
        FontScale.SMALL -> MaterialTheme.typography.bodyMedium
        FontScale.MEDIUM -> MaterialTheme.typography.titleMedium
        FontScale.LARGE -> MaterialTheme.typography.headlineSmall
    }
    HealthCard(
        modifier = modifier
            .height(Dimens.buttonHeightLarge)
            .clickable { onClick(scale) },
        shape = Shape.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(Dimens.spaceSmall),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.settings_font_size_sample),
                    style = sampleStyle,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun LanguageChoice(
    language: AppLanguage,
    selected: Boolean,
    onClick: (AppLanguage) -> Unit
) {
    val label = stringResource(
        if (language == AppLanguage.VIETNAMESE) R.string.language_vietnamese
        else R.string.language_english
    )
    val nativeLabel = stringResource(
        if (language == AppLanguage.VIETNAMESE) R.string.settings_vietnamese_native
        else R.string.settings_english_native
    )
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.buttonHeightLarge)
            .clickable { onClick(language) },
        shape = Shape.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HealthIconText(
                text = stringResource(
                    if (language == AppLanguage.VIETNAMESE) R.string.settings_icon_vietnamese
                    else R.string.settings_icon_english
                ),
                style = MaterialTheme.typography.titleLarge
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimens.spaceMedium)
            ) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier.size(Dimens.selectionIndicatorSize),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    HealthIconText(
                        text = stringResource(R.string.settings_icon_check),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

