package com.quyetbkhoa.healthtracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.os.LocaleListCompat
import com.quyetbkhoa.healthtracker.core.designsystem.HealthTrackerTheme
import com.quyetbkhoa.healthtracker.core.navigation.AppNavigation
import com.quyetbkhoa.healthtracker.domain.model.AppLanguage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureSupportedAppLanguage()
        enableEdgeToEdge()
        
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            when (val state = uiState) {
                is MainActivityUiState.Loading -> {
                    // Show a simple loading screen while DataStore is being read
                    LoadingScreen()
                }
                is MainActivityUiState.Success -> {
                    HealthTrackerTheme(
                        themeType = state.themeType
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            AppNavigation(
                                themeType = state.themeType,
                                hasProfile = state.hasProfile,
                                onThemeChanged = viewModel::setTheme,
                                onLanguageChanged = ::setAppLanguage
                            )
                        }
                    }
                }
            }
        }
    }

    private fun ensureSupportedAppLanguage() {
        val languageTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (languageTag.isBlank()) {
            setAppLanguage(AppLanguage.VIETNAMESE)
        }
    }

    private fun setAppLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.languageTag)
        )
    }
}

@Composable
fun LoadingScreen() {
    HealthTrackerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Preview
@Composable
private fun PreviewLoadingScreen() {
    LoadingScreen()
}
