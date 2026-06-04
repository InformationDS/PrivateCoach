package com.privatecoach.app.ui.screen.history

import com.privatecoach.app.core.model.Workout

data class HistoryUiState(
    val workouts: List<Workout> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)
