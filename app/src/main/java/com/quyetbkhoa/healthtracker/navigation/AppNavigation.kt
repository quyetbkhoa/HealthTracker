package com.quyetbkhoa.healthtracker.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.quyetbkhoa.healthtracker.R
import com.quyetbkhoa.healthtracker.domain.model.AppLanguage
import com.quyetbkhoa.healthtracker.domain.model.FontScale
import com.quyetbkhoa.healthtracker.presentation.onboarding.*
import com.quyetbkhoa.healthtracker.presentation.activity.AddActivityRoute
import com.quyetbkhoa.healthtracker.presentation.activityhistory.ActivityHistoryRoute
import com.quyetbkhoa.healthtracker.presentation.dashboard.DashboardRoute
import com.quyetbkhoa.healthtracker.presentation.meal.AddMealRoute
import com.quyetbkhoa.healthtracker.presentation.mealjournal.MealJournalRoute
import com.quyetbkhoa.healthtracker.presentation.profile.ProfileSettingsRoute
import com.quyetbkhoa.healthtracker.presentation.settings.SettingsRoute
import com.quyetbkhoa.healthtracker.presentation.statistics.StatisticsChartsRoute
import com.quyetbkhoa.healthtracker.presentation.statistics.StatisticsOverviewRoute
import com.quyetbkhoa.healthtracker.domain.model.MealType
import com.quyetbkhoa.healthtracker.domain.model.ReminderType
import com.quyetbkhoa.healthtracker.domain.model.ReminderSettings
import com.quyetbkhoa.healthtracker.domain.model.ReminderTime
import com.quyetbkhoa.healthtracker.domain.model.ThemeMode
import java.time.LocalDate

@Composable
fun AppNavigation(
    themeMode: ThemeMode,
    fontScale: FontScale,
    hasProfile: Boolean,
    reminderSettings: ReminderSettings,
    hasExactAlarmAccess: Boolean,
    destinationToOpen: AppDestination?,
    onThemeChanged: (ThemeMode) -> Unit,
    onFontScaleChanged: (FontScale) -> Unit,
    onLanguageChanged: (AppLanguage) -> Unit,
    onRemindersChanged: (Boolean) -> Unit,
    onReminderTimeChanged: (ReminderType, ReminderTime) -> Unit,
    onTestDinnerReminder: () -> Unit,
    onRequestExactAlarmAccess: () -> Unit,
    onDestinationConsumed: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val addMealSavedMessage = stringResource(R.string.add_meal_saved)
    val addActivitySavedMessage = stringResource(R.string.add_activity_saved)
    val profileSavedMessage = stringResource(R.string.profile_settings_saved)

    LaunchedEffect(destinationToOpen, hasProfile) {
        val destination = destinationToOpen ?: return@LaunchedEffect
        if (hasProfile) {
            val route = destination.toRoute(LocalDate.now().toEpochDay())
            navController.navigate(route) {
                launchSingleTop = true
                if (route == AppRoute.Home) {
                    popUpTo<AppRoute.Home> { inclusive = false }
                }
            }
            onDestinationConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (hasProfile) AppRoute.Home else AppRoute.Welcome
    ) {
        composable<AppRoute.Welcome> {
            WelcomeScreen(
                onStartClick = { navController.navigate(AppRoute.OnboardingGraph) }
            )
        }

        navigation<AppRoute.OnboardingGraph>(startDestination = AppRoute.ProfileStep1) {
            composable<AppRoute.ProfileStep1> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoute.OnboardingGraph)
                }
                val viewModel: ProfileSetupViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is ProfileSetupUiEvent.NavigateToStep2 ->
                                navController.navigate(AppRoute.ProfileStep2)
                            is ProfileSetupUiEvent.ShowToast -> Toast.makeText(context, event.messageRes, Toast.LENGTH_SHORT).show()
                            else -> Unit
                        }
                    }
                }

                ProfileSetupStep1Screen(
                    uiState = uiState,
                    onAction = viewModel::onAction
                )
            }

            composable<AppRoute.ProfileStep2> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(AppRoute.OnboardingGraph)
                }
                val viewModel: ProfileSetupViewModel = hiltViewModel(parentEntry)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.uiEvent.collect { event ->
                        when (event) {
                            is ProfileSetupUiEvent.NavigateToDashboard -> navController.navigate(AppRoute.Home) {
                                popUpTo<AppRoute.Welcome> { inclusive = true }
                            }
                            is ProfileSetupUiEvent.ShowToast -> Toast.makeText(context, event.messageRes, Toast.LENGTH_SHORT).show()
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

        composable<AppRoute.Home> {
            DashboardRoute(
                onNavigateToSettings = { navController.navigate(AppRoute.Settings) },
                onNavigateToAddMeal = {
                    navController.navigate(
                        AppRoute.AddMeal(LocalDate.now().toEpochDay(), MealType.BREAKFAST)
                    )
                },
                onNavigateToAddActivity = {
                    navController.navigate(AppRoute.AddActivity(LocalDate.now().toEpochDay()))
                },
                onNavigateToActivityHistory = {
                    navController.navigate(AppRoute.ActivityHistory)
                },
                onNavigateToMealJournal = { navController.navigate(AppRoute.MealJournal) },
                onNavigateToStatistics = { navController.navigate(AppRoute.Statistics) }
            )
        }

        composable<AppRoute.Statistics> {
            StatisticsOverviewRoute(
                onNavigateToCharts = { range ->
                    navController.navigate(AppRoute.StatisticsCharts(range))
                }
            )
        }

        composable<AppRoute.StatisticsCharts> {
            StatisticsChartsRoute(onNavigateBack = { navController.popBackStack() })
        }

        composable<AppRoute.AddMeal> {
            AddMealRoute(
                onNavigateBack = { navController.popBackStack() },
                onSaved = {
                    Toast.makeText(
                        context,
                        addMealSavedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.popBackStack()
                }
            )
        }

        composable<AppRoute.MealJournal> {
            MealJournalRoute(
                onNavigateBack = { navController.popBackStack() },
                onAddMeal = { epochDay, mealType ->
                    navController.navigate(AppRoute.AddMeal(epochDay, mealType))
                }
            )
        }

        composable<AppRoute.AddActivity> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.AddActivity>()
            AddActivityRoute(
                epochDay = route.epochDay,
                onNavigateBack = { navController.popBackStack() },
                onSaved = {
                    Toast.makeText(
                        context,
                        addActivitySavedMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.popBackStack()
                }
            )
        }

        composable<AppRoute.ActivityHistory> {
            ActivityHistoryRoute(
                onNavigateBack = { navController.popBackStack() },
                onAddActivity = { epochDay ->
                    navController.navigate(AppRoute.AddActivity(epochDay))
                }
            )
        }

        composable<AppRoute.Settings> {
            SettingsRoute(
                themeMode = themeMode,
                fontScale = fontScale,
                reminderSettings = reminderSettings,
                hasExactAlarmAccess = hasExactAlarmAccess,
                onThemeChanged = onThemeChanged,
                onFontScaleChanged = onFontScaleChanged,
                selectedLanguage = AppLanguage.fromLanguageTag(
                    AppCompatDelegate.getApplicationLocales().toLanguageTags()
                ),
                onLanguageChanged = onLanguageChanged,
                onRemindersChanged = onRemindersChanged,
                onReminderTimeChanged = onReminderTimeChanged,
                onTestDinnerReminder = onTestDinnerReminder,
                onRequestExactAlarmAccess = onRequestExactAlarmAccess,
                onNavigateToProfile = { navController.navigate(AppRoute.ProfileSettings) },
                onNavigateBack = { navController.popBackStack() },
                onResetCompleted = {
                    navController.navigate(AppRoute.Welcome) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<AppRoute.ProfileSettings> {
            ProfileSettingsRoute(
                onNavigateBack = { navController.popBackStack() },
                onSaved = {
                    Toast.makeText(context, profileSavedMessage, Toast.LENGTH_SHORT).show()
                    navController.navigate(AppRoute.Home) {
                        popUpTo<AppRoute.Home> { inclusive = true }
                    }
                }
            )
        }
    }
}

private fun AppDestination.toRoute(epochDay: Long): AppRoute = when (this) {
    AppDestination.DASHBOARD -> AppRoute.Home
    AppDestination.ADD_MEAL -> AppRoute.AddMeal(epochDay, MealType.BREAKFAST)
    AppDestination.ADD_LUNCH -> AppRoute.AddMeal(epochDay, MealType.LUNCH)
    AppDestination.ADD_DINNER -> AppRoute.AddMeal(epochDay, MealType.DINNER)
    AppDestination.ADD_ACTIVITY -> AppRoute.AddActivity(epochDay)
}
