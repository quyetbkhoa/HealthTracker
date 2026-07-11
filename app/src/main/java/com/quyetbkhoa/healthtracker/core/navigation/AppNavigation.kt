package com.quyetbkhoa.healthtracker.core.navigation

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.core.designsystem.AppThemeType
import com.quyetbkhoa.healthtracker.presentation.onboarding.*
import com.quyetbkhoa.healthtracker.presentation.dashboard.DashboardScreen
import com.quyetbkhoa.healthtracker.presentation.settings.SettingsScreen
import com.quyetbkhoa.healthtracker.presentation.tdee.TdeeResultScreen
import com.quyetbkhoa.healthtracker.presentation.profile.ProfileSettingsScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    themeType: AppThemeType,
    onThemeChanged: (AppThemeType) -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                onStartClick = { navController.navigate("onboarding_graph") }
            )
        }

        navigation(startDestination = "profile_step1", route = "onboarding_graph") {
            composable("profile_step1") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("onboarding_graph")
                }
                val viewModel: ProfileSetupViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is ProfileSetupUiEvent.NavigateToStep2 -> navController.navigate("profile_step2")
                            is ProfileSetupUiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                            else -> Unit
                        }
                    }
                }

                ProfileSetupStep1Screen(
                    uiState = uiState,
                    onAction = viewModel::onAction
                )
            }

            composable("profile_step2") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("onboarding_graph")
                }
                val viewModel: ProfileSetupViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is ProfileSetupUiEvent.NavigateToTdeeResult -> navController.navigate("tdee_result")
                            is ProfileSetupUiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                            else -> Unit
                        }
                    }
                }

                ProfileSetupStep2Screen(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("tdee_result") {
            TdeeResultScreen(
                onNavigateToDashboard = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            DashboardScreen(
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("settings") {
            SettingsScreen(
                themeType = themeType,
                onThemeChanged = onThemeChanged,
                onNavigateToProfile = { navController.navigate("profile_settings") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("profile_settings") {
            ProfileSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaved = {
                    Toast.makeText(context, context.getString(R.string.profile_settings_saved), Toast.LENGTH_SHORT).show()
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
