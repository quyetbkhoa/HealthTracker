# Chi tiết Cấu trúc ứng dụng HealthTracker

## File: HealthTrackerApplication.kt
- **class HealthTrackerApplication**

## File: MainActivity.kt
- **class MainActivity**
  - Hàm: `onCreate()`
  - Composable: `LoadingScreen()`

## File: MainViewModel.kt
- **class MainViewModel**
  - Hàm: `setTheme()`
- **interface MainActivityUiState**
- **object Loading**
- **class Success**

## File: core\designsystem\Dimens.kt
- **object Dimens**

## File: core\designsystem\Shape.kt
- **object Shape**

## File: core\designsystem\Theme.kt
- **enum class AppThemeType**
  - Composable: `HealthTrackerTheme()`

## File: core\designsystem\component\button\HealthPrimaryButton.kt
  - Composable: `HealthPrimaryButton()`

## File: core\designsystem\component\card\HealthCard.kt
  - Composable: `HealthCard()`
  - Composable: `HealthElevatedCard()`
  - Composable: `HealthOutlinedCard()`

## File: core\navigation\AppNavigation.kt
  - Composable: `AppNavigation()`

## File: data\datastore\DailyCalorieDataStore.kt
- **class DailyCalorieDataStore**
  - Hàm: `observeSummary()`
  - Hàm: `updateConsumedCalories()`
  - Hàm: `updateExerciseCalories()`
  - Hàm: `clear()`
  - Hàm: `updateDay()`

## File: data\datastore\ProfileDataStore.kt
- **class ProfileDataStore**
  - Hàm: `saveProfile()`
  - Hàm: `clearProfile()`

## File: data\datastore\SettingsDataStore.kt
- **class SettingsDataStore**
  - Hàm: `saveThemeType()`
  - Hàm: `saveLanguage()`

## File: data\local\HealthTrackerDatabase.kt
- **class HealthTrackerDatabase**

## File: data\local\meal\MealDao.kt
- **interface MealDao**
  - Hàm: `insert()`
  - Hàm: `update()`
  - Hàm: `delete()`
  - Hàm: `deleteById()`
  - Hàm: `deleteAll()`
  - Hàm: `observeMealsBetween()`

## File: data\local\meal\MealEntity.kt
- **class MealEntity**

## File: data\repository\DailyCalorieRepositoryImpl.kt
- **class DailyCalorieRepositoryImpl**
  - Hàm: `observeSummary()`
  - Hàm: `updateConsumedCalories()`
  - Hàm: `updateExerciseCalories()`
  - Hàm: `clear()`

## File: data\repository\MealRepositoryImpl.kt
- **class MealRepositoryImpl**
  - Hàm: `observeMealsByDay()`
  - Hàm: `addMeal()`
  - Hàm: `updateMeal()`
  - Hàm: `deleteMeal()`
  - Hàm: `clearMeals()`

## File: data\repository\ProfileRepositoryImpl.kt
- **class ProfileRepositoryImpl**
  - Hàm: `saveProfile()`
  - Hàm: `clearProfile()`

## File: data\repository\SettingsRepositoryImpl.kt
- **class SettingsRepositoryImpl**
  - Hàm: `setThemeType()`
  - Hàm: `setLanguage()`

## File: di\AppModule.kt
- **object AppModule**
  - Hàm: `provideHealthTrackerDatabase()`
  - Hàm: `provideMealDao()`
  - Hàm: `provideMealRepository()`
  - Hàm: `providePreferencesDataStore()`
  - Hàm: `provideSettingsDataStore()`
  - Hàm: `provideSettingsRepository()`
  - Hàm: `provideProfileDataStore()`
  - Hàm: `provideProfileRepository()`
  - Hàm: `provideDailyCalorieDataStore()`
  - Hàm: `provideDailyCalorieRepository()`

## File: domain\model\DailyCalorieSummary.kt
- **class DailyCalorieSummary**

## File: domain\model\MealEntry.kt
- **enum class MealType**
- **class MealEntry**

## File: domain\model\UserProfile.kt
- **class UserProfile**
- **enum class Gender**
- **enum class ActivityLevel**
- **enum class Goal**

## File: domain\repository\DailyCalorieRepository.kt
- **interface DailyCalorieRepository**
  - Hàm: `observeSummary()`
  - Hàm: `updateConsumedCalories()`
  - Hàm: `updateExerciseCalories()`
  - Hàm: `clear()`

## File: domain\repository\MealRepository.kt
- **interface MealRepository**
  - Hàm: `observeMealsByDay()`
  - Hàm: `addMeal()`
  - Hàm: `updateMeal()`
  - Hàm: `deleteMeal()`
  - Hàm: `clearMeals()`

## File: domain\repository\ProfileRepository.kt
- **interface ProfileRepository**
  - Hàm: `saveProfile()`
  - Hàm: `clearProfile()`

## File: domain\repository\SettingsRepository.kt
- **interface SettingsRepository**
  - Hàm: `setThemeType()`
  - Hàm: `setLanguage()`

## File: domain\usecase\AddMealUseCase.kt
- **enum class AddMealValidationError**
- **interface AddMealResult**
- **object Success**
- **class Invalid**
- **class AddMealUseCase**

## File: domain\usecase\CalculateBmiUseCase.kt
- **enum class BmiCategory**
- **class BmiResult**
- **class CalculateBmiUseCase**

## File: domain\usecase\CalculateTdeeUseCase.kt
- **class TdeeResult**
- **class CalculateTdeeUseCase**

## File: domain\usecase\ResetUserDataUseCase.kt
- **class ResetUserDataUseCase**

## File: presentation\dashboard\DashboardDimens.kt
- **object DashboardDimens**

## File: presentation\dashboard\DashboardScreen.kt
  - Composable: `DashboardScreen()`
  - Composable: `DashboardContent()`
  - Composable: `DashboardLoadingState()`
  - Composable: `DashboardHeader()`
  - Composable: `NotificationButton()`
  - Composable: `CalorieOverviewCard()`
  - Composable: `GoalRing()`
  - Composable: `OverviewMetric()`
  - Composable: `ProgressLine()`
  - Composable: `AchievementCard()`
  - Composable: `DashboardQuickActions()`
  - Composable: `QuickActionCard()`
  - Composable: `TodayMealsSection()`
  - Composable: `MealCard()`
  - Composable: `DailyTipCard()`
  - Composable: `DashboardBottomBar()`
  - Composable: `DashboardCard()`
  - Hàm: `formatNumber()`

## File: presentation\dashboard\DashboardViewModel.kt
- **class DashboardUiState**
- **interface DashboardAction**
- **object AddMeal**
- **object AddActivity**
- **interface DashboardUiEvent**
- **object NavigateToAddMeal**
- **class DashboardViewModel**
  - Hàm: `onAction()`

## File: presentation\meal\AddMealScreen.kt
  - Composable: `AddMealScreen()`
  - Composable: `AddMealContent()`
  - Composable: `AddMealHeader()`
  - Composable: `MealTypeSelector()`
  - Composable: `mealTypeLabel()`
  - Composable: `mealTypeIcon()`
  - Composable: `addMealErrorText()`

## File: presentation\meal\AddMealViewModel.kt
- **class AddMealUiState**
- **interface AddMealAction**
- **class UpdateName**
- **class UpdateCalories**
- **class SelectMealType**
- **object Save**
- **interface AddMealUiEvent**
- **object Saved**
- **object SaveFailed**
- **class AddMealViewModel**
  - Hàm: `onAction()`
  - Hàm: `saveMeal()`
  - Hàm: `showValidationError()`

## File: presentation\onboarding\OnboardingDimens.kt
- **object OnboardingDimens**

## File: presentation\onboarding\ProfileSetupStep1Screen.kt
  - Composable: `OnboardingSetupHeader()`
  - Composable: `StepProgress()`
  - Composable: `StepMarker()`
  - Composable: `ProfileSetupStep1Screen()`
  - Hàm: `isSelectableDate()`
  - Composable: `GenderSelector()`
  - Composable: `GenderOption()`
  - Composable: `ProfileInputField()`
  - Composable: `ProfileDateRow()`
  - Composable: `FieldError()`
  - Composable: `onboardingFieldColors()`

## File: presentation\onboarding\ProfileSetupStep2Screen.kt
  - Composable: `ProfileSetupStep2Screen()`
  - Composable: `OnboardingSectionCard()`
  - Composable: `ActivityOption()`
  - Composable: `GoalOption()`
  - Composable: `SelectionCard()`
  - Composable: `EstimateMetric()`
  - Hàm: `formatNumber()`
  - Composable: `SelectableCard()`

## File: presentation\onboarding\ProfileSetupViewModel.kt
- **class ProfileSetupUiState**
- **interface ProfileSetupAction**
- **class UpdateFullName**
- **class UpdateDateOfBirth**
- **class UpdateGender**
- **class UpdateWeight**
- **class UpdateHeight**
- **class UpdateActivityLevel**
- **class UpdateGoal**
- **class UpdateAcceptedTerms**
- **object SubmitInformation**
- **object SubmitProfile**
- **interface ProfileSetupUiEvent**
- **object NavigateToStep2**
- **object NavigateToTdeeResult**
- **class ShowToast**
- **class ProfileSetupViewModel**
  - Hàm: `onAction()`
  - Hàm: `validateInformation()`
  - Hàm: `submitProfile()`

## File: presentation\onboarding\WelcomeScreen.kt
  - Composable: `WelcomeScreen()`
  - Composable: `PreviewWelcomeScreen()`

## File: presentation\profile\ProfileSettingsScreen.kt
  - Composable: `ProfileSettingsScreen()`
  - Composable: `ProfileSettingsContent()`
  - Hàm: `isSelectableDate()`
  - Composable: `BmiCard()`
  - Composable: `ProfileTextField()`
  - Composable: `FieldTitle()`
  - Composable: `SelectableCardCompact()`

## File: presentation\profile\ProfileSettingsViewModel.kt
- **class ProfileSettingsUiState**
- **interface ProfileSettingsAction**
- **class UpdateFullName**
- **class UpdateDateOfBirth**
- **class UpdateGender**
- **class UpdateWeight**
- **class UpdateHeight**
- **class UpdateActivityLevel**
- **class UpdateGoal**
- **object Save**
- **interface ProfileSettingsEvent**
- **object Saved**
- **class ProfileSettingsViewModel**
  - Hàm: `onAction()`
  - Hàm: `loadProfile()`
  - Hàm: `updateBodyValues()`
  - Hàm: `save()`

## File: presentation\settings\SettingsScreen.kt
  - Composable: `SettingsScreen()`
  - Composable: `ResetConfirmationDialog()`

## File: presentation\settings\SettingsViewModel.kt
- **class SettingsUiState**
- **interface SettingsAction**
- **object RequestReset**
- **object CancelReset**
- **object ConfirmReset**
- **interface SettingsUiEvent**
- **object ResetCompleted**
- **object ResetFailed**
- **class SettingsViewModel**
  - Hàm: `onAction()`
  - Hàm: `resetData()`

## File: presentation\tdee\TdeeResultScreen.kt
  - Composable: `TdeeResultScreen()`
  - Composable: `TdeeResultContent()`
  - Composable: `ResultMetricCard()`

## File: presentation\tdee\TdeeResultViewModel.kt
- **interface TdeeResultUiState**
- **object Loading**
- **class Success**
- **interface TdeeResultAction**
- **class UpdateTarget**
- **object Save**
- **interface TdeeResultEvent**
- **object NavigateToDashboard**
- **class TdeeResultViewModel**
  - Hàm: `onAction()`
  - Hàm: `save()`

## File: presentation\test\TestScreens.kt
  - Composable: `TestHomeScreen()`

