package com.quyetbkhoa.healthtracker.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.AppThemeType
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.designsystem.Shape
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton
import com.quyetbkhoa.healthtracker.domain.model.AppLanguage

@Composable
fun SettingsScreen(
    themeType: AppThemeType,
    onThemeChanged: (AppThemeType) -> Unit,
    selectedLanguage: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    onResetCompleted: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val resetFailedMessage = stringResource(R.string.settings_reset_failed)

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.LanguageSelected -> onLanguageChanged(event.language)
                SettingsUiEvent.ResetCompleted -> onResetCompleted()
                SettingsUiEvent.ResetFailed -> Toast.makeText(
                    context,
                    resetFailedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    if (uiState.showResetConfirmation) {
        ResetConfirmationDialog(
            isResetting = uiState.isResetting,
            onConfirm = { viewModel.onAction(SettingsAction.ConfirmReset) },
            onDismiss = { viewModel.onAction(SettingsAction.CancelReset) }
        )
    }

    SettingsContent(
        themeType = themeType,
        selectedLanguage = selectedLanguage,
        isResetting = uiState.isResetting,
        onThemeChanged = onThemeChanged,
        onLanguageChanged = { viewModel.onAction(SettingsAction.SelectLanguage(it)) },
        onNavigateToProfile = onNavigateToProfile,
        onNavigateBack = onNavigateBack,
        onReset = { viewModel.onAction(SettingsAction.RequestReset) }
    )
}

@Composable
private fun SettingsContent(
    themeType: AppThemeType,
    selectedLanguage: AppLanguage,
    isResetting: Boolean,
    onThemeChanged: (AppThemeType) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateBack: () -> Unit,
    onReset: () -> Unit
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = { SettingsHeader(onNavigateBack) }
        ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMedium)
        ) {
//            item { SettingsHeader(onNavigateBack) }
            item {
                SettingsSectionCard(
                    icon = stringResource(R.string.settings_icon_profile),
                    title = stringResource(R.string.settings_account_section)
                ) {
                    SettingsNavigationCard(
                        icon = stringResource(R.string.settings_icon_profile),
                        title = stringResource(R.string.profile_settings_open),
                        subtitle = stringResource(R.string.settings_profile_description),
                        onClick = onNavigateToProfile
                    )
                }
            }
            item {
                SettingsSectionCard(
                    icon = stringResource(R.string.settings_icon_appearance),
                    title = stringResource(R.string.settings_appearance_section)
                ) {

                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                            ThemeChoice(
                                type = AppThemeType.LIGHT,
                                selected = themeType == AppThemeType.LIGHT,
                                modifier = Modifier.weight(1f),
                                onClick = onThemeChanged
                            )
                            ThemeChoice(
                                type = AppThemeType.DARK,
                                selected = themeType == AppThemeType.DARK,
                                modifier = Modifier.weight(1f),
                                onClick = onThemeChanged
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSmall)) {
                            ThemeChoice(
                                type = AppThemeType.PINK,
                                selected = themeType == AppThemeType.PINK,
                                modifier = Modifier.weight(1f),
                                onClick = onThemeChanged
                            )
                            ThemeChoice(
                                type = AppThemeType.SYSTEM,
                                selected = themeType == AppThemeType.SYSTEM,
                                modifier = Modifier.weight(1f),
                                onClick = onThemeChanged
                            )
                        }
                    }
                }
            }
            item {
                SettingsSectionCard(
                    icon = stringResource(R.string.settings_icon_language),
                    title = stringResource(R.string.settings_language_section)
                ) {
                    LanguageChoice(
                        language = AppLanguage.VIETNAMESE,
                        selected = selectedLanguage == AppLanguage.VIETNAMESE,
                        onClick = onLanguageChanged
                    )
                    Spacer(Modifier.height(Dimens.spaceSmall))
                    LanguageChoice(
                        language = AppLanguage.ENGLISH,
                        selected = selectedLanguage == AppLanguage.ENGLISH,
                        onClick = onLanguageChanged
                    )
                }
            }
            item {
                SettingsSectionCard(
                    icon = stringResource(R.string.settings_icon_data),
                    title = stringResource(R.string.settings_data_section)
                ) {
                    DangerZoneCard(
                        isResetting = isResetting,
                        onClick = onReset
                    )
                }
            }
            item { Spacer(Modifier.height(Dimens.spaceLarge)) }
        }
    }
}

@Composable
private fun SettingsHeader(onNavigateBack: () -> Unit) {
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
        Column(modifier = Modifier.padding(start = Dimens.spaceSmall)) {
            Text(
                text = stringResource(R.string.screen_settings),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    icon: String,
    title: String,
    content: @Composable ColumnScope.() -> Unit
){
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = Shape.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Dimens.cardElevationMedium
        )
    ){
        Column(
            modifier = Modifier.padding(Dimens.spaceMedium)
        ){
            //title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, style = MaterialTheme.typography.titleLarge)
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
private fun SettingsNavigationCard(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = Shape.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevationMedium),
        border = BorderStroke(width = Dimens.borderWidthThin/2, color = MaterialTheme.colorScheme.primary)
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
            Text(
                text = stringResource(R.string.settings_icon_chevron),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ThemeChoice(
    type: AppThemeType,
    selected: Boolean,
    modifier: Modifier,
    onClick: (AppThemeType) -> Unit
) {
    val label = stringResource(
        when (type) {
            AppThemeType.LIGHT -> R.string.theme_light
            AppThemeType.DARK -> R.string.theme_dark
            AppThemeType.PINK -> R.string.theme_pink
            AppThemeType.SYSTEM -> R.string.theme_system
        }
    )
    val icon = stringResource(
        when (type) {
            AppThemeType.LIGHT -> R.string.settings_icon_light
            AppThemeType.DARK -> R.string.settings_icon_dark
            AppThemeType.PINK -> R.string.settings_icon_pink
            AppThemeType.SYSTEM -> R.string.settings_icon_system
        }
    )
    Card(
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
                .height(Dimens.buttonHeightLarge)
                .padding(Dimens.spaceMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            Text(icon, style = MaterialTheme.typography.titleLarge)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LanguageChoice(
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.buttonHeightLarge)
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
            Text(
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
                    Text(
                        text = stringResource(R.string.settings_icon_check),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DangerZoneCard(isResetting: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isResetting, onClick = onClick),
        shape = Shape.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIconBubble(
                icon = stringResource(R.string.settings_icon_reset),
                isError = true
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Dimens.spaceMedium)
            ) {
                Text(
                    stringResource(R.string.settings_reset_data),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    stringResource(R.string.settings_reset_description),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            if (isResetting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimens.iconSizeMedium),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            } else {
                Text(
                    stringResource(R.string.settings_icon_chevron),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun SettingsIconBubble(
    icon: String,
    isPrimary: Boolean = false,
    isError: Boolean = false
) {
    val containerColor = when {
        isError -> MaterialTheme.colorScheme.error
        isPrimary -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = when {
        isError -> MaterialTheme.colorScheme.onError
        isPrimary -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    Box(
        modifier = Modifier
            .size(Dimens.buttonHeightMedium)
            .padding(Dimens.spaceExtraSmall),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = Shape.pill,
            colors = CardDefaults.cardColors(containerColor = containerColor)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(icon, color = contentColor, style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun ResetConfirmationDialog(
    isResetting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isResetting) onDismiss() },
        title = { Text(stringResource(R.string.settings_reset_title)) },
        text = { Text(stringResource(R.string.settings_reset_message)) },
        confirmButton = {
            HealthPrimaryButton(
                onClick = onConfirm,
                modifier = Modifier.width(Dimens.dialogActionWidth),
                enabled = !isResetting
            ) {
                if (isResetting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimens.iconSizeMedium),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = Dimens.borderWidthThick
                    )
                } else {
                    Text(stringResource(R.string.settings_reset_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isResetting) {
                Text(stringResource(R.string.settings_reset_cancel))
            }
        }
    )
}

@Preview
@Composable
private fun PreviewSettingsScreen() {
    HealthTrackerTheme {
        SettingsContent(
            themeType = AppThemeType.LIGHT,
            selectedLanguage = AppLanguage.VIETNAMESE,
            isResetting = false,
            onThemeChanged = {},
            onLanguageChanged = {},
            onNavigateToProfile = {},
            onNavigateBack = {},
            onReset = {}
        )
    }
}
