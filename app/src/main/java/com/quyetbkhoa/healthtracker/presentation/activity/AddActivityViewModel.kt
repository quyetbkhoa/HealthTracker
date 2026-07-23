package com.quyetbkhoa.healthtracker.presentation.activity

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quyetbkhoa.healthtracker.domain.model.PhysicalActivityType
import com.quyetbkhoa.healthtracker.domain.repository.ActivityRepository
import com.quyetbkhoa.healthtracker.domain.repository.ProfileRepository
import com.quyetbkhoa.healthtracker.domain.usecase.AddActivityRecordError
import com.quyetbkhoa.healthtracker.domain.usecase.AddActivityRecordResult
import com.quyetbkhoa.healthtracker.domain.usecase.AddActivityRecordUseCase
import com.quyetbkhoa.healthtracker.domain.usecase.CalculateActivityCaloriesUseCase
import com.quyetbkhoa.healthtracker.domain.usecase.EnsureDefaultActivitiesUseCase
import com.quyetbkhoa.healthtracker.domain.usecase.SetActivityFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.roundToInt

@Immutable
data class ActivityItemUiModel(
    val id: Long,
    val name: String,
    val met: Double,
    val iconName: String,
    val isFavorite: Boolean
)

@Immutable
data class AddActivityUiState(
    val epochDay: Long = LocalDate.now().toEpochDay(),
    val activities: List<ActivityItemUiModel> = emptyList(),
    val selectedActivityId: Long? = null,
    val durationMinutes: Int = AddActivityViewModel.DEFAULT_DURATION_MINUTES,
    val weightKg: Double? = null,
    val estimatedCalories: Double = 0.0,
    val isActivityPickerExpanded: Boolean = true,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: AddActivityError? = null
) {
    val selectedActivity: ActivityItemUiModel?
        get() = activities.firstOrNull { it.id == selectedActivityId }
    val displayedCalories: Int get() = estimatedCalories.roundToInt()
    val canSave: Boolean
        get() = selectedActivity != null &&
            durationMinutes in AddActivityViewModel.MIN_DURATION_MINUTES..
                AddActivityViewModel.MAX_DURATION_MINUTES &&
            weightKg?.let { it in 1.0..300.0 } == true &&
            estimatedCalories > 0.0 &&
            !isSaving
}

enum class AddActivityError {
    NO_PROFILE_WEIGHT,
    INVALID_DURATION,
    INVALID_ACTIVITY,
    SAVE_FAILED
}

sealed interface AddActivityAction {
    data class SetDate(val epochDay: Long) : AddActivityAction
    data class SelectActivity(val activityId: Long) : AddActivityAction
    data class ToggleFavorite(val activityId: Long) : AddActivityAction
    data class ChangeDuration(val minutes: Int) : AddActivityAction
    data object ReselectActivity : AddActivityAction
    data object SaveActivity : AddActivityAction
}

sealed interface AddActivityUiEvent {
    data object Saved : AddActivityUiEvent
}

@HiltViewModel
class AddActivityViewModel @Inject constructor(
    activityRepository: ActivityRepository,
    profileRepository: ProfileRepository,
    private val calculateCalories: CalculateActivityCaloriesUseCase,
    private val addActivityRecord: AddActivityRecordUseCase,
    private val ensureDefaultActivities: EnsureDefaultActivitiesUseCase,
    private val setActivityFavorite: SetActivityFavoriteUseCase
) : ViewModel() {
    private val editorState = MutableStateFlow(EditorState())
    private var isSaveInProgress = false
    private var hasSaved = false
    private val uiEventChannel = Channel<AddActivityUiEvent>(Channel.BUFFERED)
    val uiEvent = uiEventChannel.receiveAsFlow()

    val uiState: StateFlow<AddActivityUiState> = combine(
        activityRepository.observeActivityTypes(),
        profileRepository.userProfile,
        editorState
    ) { activities, profile, editor ->
        val items = activities.map(PhysicalActivityType::toUiModel)
        val selected = items.firstOrNull { it.id == editor.selectedActivityId }
        val weight = profile?.weightKg?.toDouble()?.takeIf { it in 1.0..300.0 }
        AddActivityUiState(
            epochDay = editor.epochDay,
            activities = items,
            selectedActivityId = selected?.id,
            durationMinutes = editor.durationMinutes,
            weightKg = weight,
            estimatedCalories = selected?.let {
                calculateCalories(it.met, weight ?: 0.0, editor.durationMinutes)
            } ?: 0.0,
            isActivityPickerExpanded = editor.isPickerExpanded || selected == null,
            isLoading = false,
            isSaving = editor.isSaving,
            error = editor.error ?: if (profile != null && weight == null) {
                AddActivityError.NO_PROFILE_WEIGHT
            } else {
                null
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddActivityUiState()
    )

    init {
        viewModelScope.launch {
            runCatching { ensureDefaultActivities() }
                .onFailure {
                    editorState.update { state -> state.copy(error = AddActivityError.SAVE_FAILED) }
                }
        }
    }

    fun onAction(action: AddActivityAction) {
        when (action) {
            is AddActivityAction.SetDate -> editorState.update {
                it.copy(epochDay = action.epochDay)
            }
            is AddActivityAction.SelectActivity -> editorState.update {
                it.copy(
                    selectedActivityId = action.activityId,
                    isPickerExpanded = false,
                    error = null
                )
            }
            is AddActivityAction.ChangeDuration -> editorState.update {
                it.copy(
                    durationMinutes = action.minutes.coerceIn(
                        SLIDER_MIN_DURATION_MINUTES,
                        SLIDER_MAX_DURATION_MINUTES
                    ),
                    error = null
                )
            }
            AddActivityAction.ReselectActivity -> editorState.update {
                it.copy(isPickerExpanded = true, error = null)
            }
            is AddActivityAction.ToggleFavorite -> toggleFavorite(action.activityId)
            AddActivityAction.SaveActivity -> saveActivity()
        }
    }

    private fun toggleFavorite(activityId: Long) {
        val activity = uiState.value.activities.firstOrNull { it.id == activityId } ?: return
        viewModelScope.launch {
            runCatching { setActivityFavorite(activityId, !activity.isFavorite) }
                .onFailure {
                    editorState.update { state -> state.copy(error = AddActivityError.SAVE_FAILED) }
                }
        }
    }

    private fun saveActivity() {
        val state = uiState.value
        if (isSaveInProgress || hasSaved) return
        val activity = state.selectedActivity
        val weight = state.weightKg
        val validationError = when {
            activity == null -> AddActivityError.INVALID_ACTIVITY
            weight == null -> AddActivityError.NO_PROFILE_WEIGHT
            state.durationMinutes !in MIN_DURATION_MINUTES..MAX_DURATION_MINUTES ->
                AddActivityError.INVALID_DURATION
            state.estimatedCalories <= 0.0 -> AddActivityError.INVALID_ACTIVITY
            else -> null
        }
        if (validationError != null) {
            editorState.update { it.copy(error = validationError) }
            return
        }

        isSaveInProgress = true
        editorState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            runCatching {
                addActivityRecord(
                    activityTypeId = checkNotNull(activity).id,
                    met = activity.met,
                    weightKg = checkNotNull(weight),
                    durationMinutes = state.durationMinutes,
                    epochDay = state.epochDay
                )
            }.onSuccess { result ->
                when (result) {
                    AddActivityRecordResult.Success -> {
                        hasSaved = true
                        uiEventChannel.send(AddActivityUiEvent.Saved)
                    }
                    is AddActivityRecordResult.Invalid -> editorState.update {
                        state -> state.copy(error = result.error.toUiError())
                    }
                }
            }.onFailure {
                editorState.update { it.copy(error = AddActivityError.SAVE_FAILED) }
            }
            isSaveInProgress = false
            editorState.update { it.copy(isSaving = false) }
        }
    }

    private data class EditorState(
        val epochDay: Long = LocalDate.now().toEpochDay(),
        val selectedActivityId: Long? = null,
        val durationMinutes: Int = DEFAULT_DURATION_MINUTES,
        val isPickerExpanded: Boolean = true,
        val isSaving: Boolean = false,
        val error: AddActivityError? = null
    )

    companion object {
        const val MIN_DURATION_MINUTES = 1
        const val MAX_DURATION_MINUTES = 600
        const val SLIDER_MIN_DURATION_MINUTES = 5
        const val SLIDER_MAX_DURATION_MINUTES = 180
        const val DURATION_STEP_MINUTES = 5
        const val DEFAULT_DURATION_MINUTES = 30
    }
}

private fun AddActivityRecordError.toUiError(): AddActivityError = when (this) {
    AddActivityRecordError.INVALID_ACTIVITY -> AddActivityError.INVALID_ACTIVITY
    AddActivityRecordError.INVALID_DURATION -> AddActivityError.INVALID_DURATION
    AddActivityRecordError.INVALID_WEIGHT -> AddActivityError.NO_PROFILE_WEIGHT
}

private fun PhysicalActivityType.toUiModel() = ActivityItemUiModel(
    id = id,
    name = name,
    met = met,
    iconName = iconName,
    isFavorite = isFavorite
)
