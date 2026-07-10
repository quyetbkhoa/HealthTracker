package com.quyetbkhoa.healthtracker.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.AppThemeType
import com.quyetbkhoa.healthtracker.core.designsystem.Dimens
import com.quyetbkhoa.healthtracker.core.designsystem.component.button.HealthPrimaryButton

@Composable
fun SettingsScreen(
    themeType: AppThemeType,
    onThemeChanged: (AppThemeType) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.screen_settings),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(Dimens.spaceExtraLarge))

        Text(
            text = stringResource(id = R.string.settings_select_theme),
            color = MaterialTheme.colorScheme.onBackground
        )

        AppThemeType.entries.forEach { type ->
            val labelRes = when (type) {
                AppThemeType.LIGHT -> R.string.theme_light
                AppThemeType.DARK -> R.string.theme_dark
                AppThemeType.PINK -> R.string.theme_pink
                AppThemeType.SYSTEM -> R.string.theme_system
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = themeType == type,
                    onClick = { onThemeChanged(type) }
                )
                Text(
                    text = stringResource(id = labelRes),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spaceExtraLarge))

        HealthPrimaryButton(onClick = onNavigateToProfile) {
            Text(text = stringResource(id = R.string.profile_settings_open))
        }

        Spacer(modifier = Modifier.height(Dimens.spaceMedium))

        HealthPrimaryButton(onClick = onNavigateBack) {
            Text(text = stringResource(id = R.string.settings_back))
        }
    }
}
