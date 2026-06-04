package com.privatecoach.app.ui.screen.calendar

import com.privatecoach.app.core.model.Workout
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val currentWeekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val isWeekView: Boolean = false,
    val selectedDate: LocalDate? = null,
    val workoutDates: Set<LocalDate> = emptySet(),
    val selectedDayWorkouts: List<Workout> = emptyList(),
    val weeklyWorkouts: Map<LocalDate, List<Workout>> = emptyMap(),
    val monthlyTrainingDays: Int = 0,
    val monthlyTotalSets: Int = 0,
    val isLoading: Boolean = true
)
