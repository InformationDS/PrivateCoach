package com.privatecoach.app.ui.screen.dashboard

import com.privatecoach.app.core.model.Workout
import java.time.LocalDate

data class DashboardUiState(
    val today: LocalDate = LocalDate.now(),
    val trainingDaysThisWeek: Int = 0,
    val lastWorkoutDaysAgo: Int? = null,
    val hasWorkoutToday: Boolean = false,
    val recentWorkouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true
)
