package com.privatecoach.app.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecoach.app.core.model.TrainingTemplate
import com.privatecoach.app.core.model.TemplateExercise
import com.privatecoach.app.domain.repository.TemplateRepository
import com.privatecoach.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val workoutId: Long = savedStateHandle.get<Long>("workoutId") ?: 0L

    private val _uiState = MutableStateFlow(WorkoutDetailUiState())
    val uiState: StateFlow<WorkoutDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            workoutRepository.getWorkoutById(workoutId).collect { workout ->
                _uiState.update { it.copy(workout = workout, isLoading = false) }
            }
        }
    }

    fun showDeleteDialog() = _uiState.update { it.copy(showDeleteDialog = true) }
    fun dismissDeleteDialog() = _uiState.update { it.copy(showDeleteDialog = false) }

    fun deleteWorkout() {
        viewModelScope.launch {
            workoutRepository.deleteWorkout(workoutId)
            _uiState.update { it.copy(showDeleteDialog = false, deleted = true) }
        }
    }

    fun saveAsTemplate() {
        val workout = _uiState.value.workout ?: return
        viewModelScope.launch {
            val template = TrainingTemplate(
                name = "${workout.bodyPart ?: "训练"}${if (workout.type == com.privatecoach.app.core.model.WorkoutType.STRENGTH) "力量" else "有氧"}",
                type = workout.type,
                bodyPart = workout.bodyPart,
                exercises = workout.exercises.mapIndexed { i, ex ->
                    TemplateExercise(name = ex.name, weightUnit = ex.weightUnit, sortOrder = i, notes = ex.notes)
                }
            )
            templateRepository.saveTemplate(template)
            _uiState.update { it.copy(saveTemplateComplete = true) }
        }
    }
}
