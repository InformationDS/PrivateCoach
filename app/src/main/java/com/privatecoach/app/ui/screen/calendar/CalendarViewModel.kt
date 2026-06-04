package com.privatecoach.app.ui.screen.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecoach.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        val now = LocalDate.now()
        _uiState.update {
            it.copy(currentWeekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
        }
        loadMonth(YearMonth.now())
    }

    fun loadMonth(yearMonth: YearMonth) {
        _uiState.update { it.copy(currentMonth = yearMonth, selectedDate = null, selectedDayWorkouts = emptyList(), isLoading = true) }
        viewModelScope.launch {
            val start = yearMonth.atDay(1)
            val end = yearMonth.atEndOfMonth()
            val dates = workoutRepository.getDistinctWorkoutDates(start, end)
            val trainingDays = workoutRepository.getTrainingDaysCount(start, end)
            _uiState.update {
                it.copy(
                    workoutDates = dates.toSet(),
                    monthlyTrainingDays = trainingDays,
                    isLoading = false
                )
            }
        }
    }

    fun previousMonth() {
        loadMonth(_uiState.value.currentMonth.minusMonths(1))
    }

    fun nextMonth() {
        loadMonth(_uiState.value.currentMonth.plusMonths(1))
    }

    fun previousWeek() {
        val newStart = _uiState.value.currentWeekStart.minusWeeks(1)
        _uiState.update { it.copy(currentWeekStart = newStart, selectedDate = null) }
        loadWeekData(newStart)
    }

    fun nextWeek() {
        val newStart = _uiState.value.currentWeekStart.plusWeeks(1)
        _uiState.update { it.copy(currentWeekStart = newStart, selectedDate = null) }
        loadWeekData(newStart)
    }

    fun toggleViewMode() {
        val newMode = !_uiState.value.isWeekView
        _uiState.update { it.copy(isWeekView = newMode, selectedDate = null) }
        if (newMode) {
            loadWeekData(_uiState.value.currentWeekStart)
        } else {
            loadMonth(_uiState.value.currentMonth)
        }
    }

    private fun loadWeekData(weekStart: LocalDate) {
        viewModelScope.launch {
            val weekEnd = weekStart.plusDays(6)
            val dates = workoutRepository.getDistinctWorkoutDates(weekStart, weekEnd)
            _uiState.update {
                it.copy(
                    workoutDates = dates.toSet(),
                    isLoading = false
                )
            }
            // Load workouts for each day of the week
            val dayWorkouts = mutableMapOf<LocalDate, List<com.privatecoach.app.core.model.Workout>>()
            for (day in 0..6) {
                val dayDate = weekStart.plusDays(day.toLong())
                workoutRepository.getWorkoutsByDateRange(dayDate, dayDate).firstOrNull()
                    ?.let { workouts ->
                    if (workouts.isNotEmpty()) {
                        dayWorkouts[dayDate] = workouts
                    }
                }
            }
            _uiState.update { it.copy(weeklyWorkouts = dayWorkouts) }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, selectedDayWorkouts = emptyList()) }
        viewModelScope.launch {
            workoutRepository.getWorkoutsByDateRange(date, date).collect { workouts ->
                _uiState.update { it.copy(selectedDayWorkouts = workouts) }
            }
        }
    }
}
