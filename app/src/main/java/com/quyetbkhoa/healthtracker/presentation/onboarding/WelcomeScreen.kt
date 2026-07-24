package com.quyetbkhoa.healthtracker.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.HealthMarqueeText as Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.presentation.designsystem.Dimens
import com.quyetbkhoa.healthtracker.presentation.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.presentation.designsystem.component.button.HealthPrimaryButton

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = Dimens.spaceLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = com.quyetbkhoa.healthtracker.presentation.designsystem.Shape.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 10.dp
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher_foreground),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier
                            .padding(Dimens.spaceMedium)
                            .height(128.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.height(Dimens.spaceExtraLarge))
                Text(
                    text = stringResource(id = R.string.welcome_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Dimens.spaceMedium))
                Text(
                    text = stringResource(id = R.string.welcome_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            HealthPrimaryButton(
                onClick = onStartClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.spaceLarge, vertical = Dimens.topBarSpacing)
                    .height(Dimens.buttonHeightMedium)
            ) {
                Text(
                    text = stringResource(id = R.string.welcome_start),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewWelcomeScreen() {
    HealthTrackerTheme {
        WelcomeScreen(onStartClick = {})
    }
}
