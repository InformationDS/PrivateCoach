package com.privatecoach.app.ui.screen.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecoach.app.core.model.BodyPart
import com.privatecoach.app.core.model.Exercise
import com.privatecoach.app.core.model.InputMode
import com.privatecoach.app.core.model.Workout
import com.privatecoach.app.core.model.WorkoutType
import com.privatecoach.app.domain.repository.TemplateRepository
import com.privatecoach.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualEntryUiState())
    val uiState: StateFlow<ManualEntryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            templateRepository.getAllTemplates().collect { templates ->
                _uiState.update { it.copy(templates = templates) }
            }
        }
    }

    fun setType(type: WorkoutType) = _uiState.update { it.copy(workoutType = type) }

    fun updateExerciseName(index: Int, name: String) {
        _uiState.update { state ->
            val exercises = state.exercises.toMutableList()
            if (index < exercises.size) exercises[index] = exercises[index].copy(name = name)
            state.copy(exercises = exercises)
        }
    }

    fun updateExerciseWeight(index: Int, weight: String) {
        _uiState.update { state ->
            val exercises = state.exercises.toMutableList()
            if (index < exercises.size) exercises[index] = exercises[index].copy(weight = weight.toDoubleOrNull())
            state.copy(exercises = exercises)
        }
    }

    fun updateExerciseSets(index: Int, sets: String) {
        _uiState.update { state ->
            val exercises = state.exercises.toMutableList()
            if (index < exercises.size) exercises[index] = exercises[index].copy(sets = sets.toIntOrNull())
            state.copy(exercises = exercises)
        }
    }

    fun updateExerciseReps(index: Int, reps: String) {
        _uiState.update { state ->
            val exercises = state.exercises.toMutableList()
            if (index < exercises.size) exercises[index] = exercises[index].copy(reps = reps.toIntOrNull())
            state.copy(exercises = exercises)
        }
    }

    fun addExercise() {
        _uiState.update { state ->
            state.copy(exercises = state.exercises + Exercise(name = "", weightUnit = "kg"))
        }
    }

    fun removeExercise(index: Int) {
        _uiState.update { state ->
            if (state.exercises.size <= 1) state
            else state.copy(exercises = state.exercises.toMutableList().also { it.removeAt(index) })
        }
    }

    fun setExerciseFeeling(index: Int, feeling: String?) {
        _uiState.update { state ->
            val exs = state.exercises.toMutableList()
            if (index < exs.size) {
                exs[index] = exs[index].copy(
                    feeling = feeling?.let {
                        try { com.privatecoach.app.core.model.Feeling.valueOf(it.uppercase()) }
                        catch (_: Exception) { null }
                    }
                )
            }
            state.copy(exercises = exs)
        }
    }
    fun setBodyPart(part: BodyPart?) = _uiState.update { it.copy(bodyPart = part) }

    fun applyTemplate(templateId: Long?) {
        if (templateId == null) {
            _uiState.update { it.copy(selectedTemplateId = null) }
            return
        }
        viewModelScope.launch {
            val template = templateRepository.getTemplateById(templateId) ?: return@launch
            _uiState.update { state ->
                state.copy(
                    selectedTemplateId = templateId,
                    workoutType = template.type,
                    bodyPart = template.bodyPart,
                    exercises = template.exercises.mapIndexed { i, te ->
                        Exercise(name = te.name, weightUnit = te.weightUnit, sortOrder = i, notes = te.notes)
                    }
                )
            }
        }
    }

    fun save() {
        val state = _uiState.value
        val validExercises = state.exercises.filter { it.name.isNotBlank() }
        if (validExercises.isEmpty()) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val workout = Workout(
                date = LocalDate.now(),
                type = state.workoutType,
                bodyPart = state.bodyPart,
                inputMode = InputMode.MANUAL,
                templateId = state.selectedTemplateId,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                exercises = validExercises
            )
            workoutRepository.saveWorkout(workout)
            _uiState.update { it.copy(isSaving = false, saveComplete = true) }
        }
    }
}
