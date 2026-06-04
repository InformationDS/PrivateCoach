package com.privatecoach.app.ui.screen.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecoach.app.core.model.AiParsedResult
import com.privatecoach.app.core.model.CardioDetail
import com.privatecoach.app.core.model.Exercise
import com.privatecoach.app.core.model.Feeling
import com.privatecoach.app.core.model.InputMode
import com.privatecoach.app.core.model.ParsedExercise
import com.privatecoach.app.core.model.Workout
import com.privatecoach.app.core.model.WorkoutType
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
class ConfirmResultViewModel @Inject constructor(
    private val resultHolder: AiResultHolder,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmResultUiState())
    val uiState: StateFlow<ConfirmResultUiState> = _uiState.asStateFlow()

    init {
        val (result, audioPath) = resultHolder.consume()
        if (result != null) {
            _uiState.update {
                it.copy(
                    parsedResult = result,
                    exercises = result.exercises,
                    bodyPart = result.bodyPart ?: "",
                    feeling = result.feeling?.name?.lowercase(),
                    audioFilePath = audioPath
                )
            }
        }
    }

    fun updateExerciseName(index: Int, name: String) {
        _uiState.update { state ->
            val exs = state.exercises.toMutableList()
            if (index < exs.size) exs[index] = exs[index].copy(name = name)
            state.copy(exercises = exs)
        }
    }

    fun updateExerciseWeight(index: Int, weight: String) {
        _uiState.update { state ->
            val exs = state.exercises.toMutableList()
            if (index < exs.size) exs[index] = exs[index].copy(weight = weight.toDoubleOrNull())
            state.copy(exercises = exs)
        }
    }

    fun updateExerciseSets(index: Int, sets: String) {
        _uiState.update { state ->
            val exs = state.exercises.toMutableList()
            if (index < exs.size) exs[index] = exs[index].copy(sets = sets.toIntOrNull())
            state.copy(exercises = exs)
        }
    }

    fun updateExerciseReps(index: Int, reps: String) {
        _uiState.update { state ->
            val exs = state.exercises.toMutableList()
            if (index < exs.size) exs[index] = exs[index].copy(reps = reps.toIntOrNull())
            state.copy(exercises = exs)
        }
    }

    fun setBodyPart(part: String) = _uiState.update { it.copy(bodyPart = part) }
    fun setFeeling(feeling: String?) = _uiState.update { it.copy(feeling = feeling) }

    fun saveWorkout() {
        val state = _uiState.value
        val parsed = state.parsedResult ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val workout = Workout(
                date = LocalDate.now(),
                type = parsed.type,
                bodyPart = state.bodyPart.ifBlank { null },
                feeling = state.feeling?.let {
                    try { Feeling.valueOf(it.uppercase()) } catch (_: Exception) { null }
                },
                aiSummary = parsed.summaryMarkdown,
                rawTranscript = null,  // could store original transcript if available
                audioFilePath = state.audioFilePath,
                inputMode = if (state.audioFilePath != null) InputMode.VOICE else InputMode.TEXT,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                exercises = state.exercises.mapIndexed { index, pe ->
                    Exercise(
                        name = pe.name,
                        weight = pe.weight,
                        weightUnit = pe.weightUnit,
                        sets = pe.sets,
                        reps = pe.reps,
                        duration = pe.duration,
                        distance = pe.distance,
                        sortOrder = index,
                        cardioDetail = parsed.cardioDetail?.let { cd ->
                            CardioDetail(
                                durationSeconds = cd.duration ?: pe.duration ?: 0,
                                distanceKm = cd.distance ?: pe.distance,
                                avgHeartRate = cd.avgHeartRate,
                                calories = cd.calories,
                                cardioType = cd.cardioType
                            )
                        }
                    )
                }
            )
            workoutRepository.createWorkout(workout)
            _uiState.update { it.copy(isSaving = false, saveComplete = true) }
        }
    }
}
