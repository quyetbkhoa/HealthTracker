package com.quyetbkhoa.healthtracker.core.navigation

import android.widget.Toast
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
import com.quyetbkhoa.healthtracker.core.designsystem.AppThemeType
import com.quyetbkhoa.healthtracker.presentation.onboarding.*
import com.quyetbkhoa.healthtracker.presentation.test.TestHomeScreen
import com.quyetbkhoa.healthtracker.presentation.settings.SettingsScreen

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
                            is ProfileSetupUiEvent.NavigateToStep3 -> navController.navigate("profile_step3")
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

            composable("profile_step3") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("onboarding_graph")
                }
                val viewModel: ProfileSetupViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is ProfileSetupUiEvent.NavigateToHome -> {
                                navController.navigate("home") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                            is ProfileSetupUiEvent.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                            else -> Unit
                        }
                    }
                }

                ProfileSetupStep3Screen(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("home") {
            TestHomeScreen(
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("settings") {
            SettingsScreen(
                themeType = themeType,
                onThemeChanged = onThemeChanged,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
