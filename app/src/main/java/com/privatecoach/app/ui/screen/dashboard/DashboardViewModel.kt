package com.privatecoach.app.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecoach.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

            val trainingDays = workoutRepository.getTrainingDaysCount(weekStart, weekEnd)
            val hasWorkoutToday = workoutRepository.getDistinctWorkoutDates(today, today).isNotEmpty()

            _uiState.update { state ->
                state.copy(
                    today = today,
                    trainingDaysThisWeek = trainingDays,
                    hasWorkoutToday = hasWorkoutToday,
                    isLoading = false
                )
            }
        }

        // Recent workouts via Flow
        viewModelScope.launch {
            workoutRepository.getRecentWorkouts(5).collect { workouts ->
                _uiState.update { state ->
                    state.copy(
                        recentWorkouts = workouts,
                        lastWorkoutDaysAgo = workouts.firstOrNull()?.let { w ->
                            java.time.temporal.ChronoUnit.DAYS.between(w.date, LocalDate.now()).toInt()
                        }
                    )
                }
            }
        }
    }
}
