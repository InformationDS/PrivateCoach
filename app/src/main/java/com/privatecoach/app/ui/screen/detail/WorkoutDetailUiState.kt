package com.privatecoach.app.ui.screen.detail

import com.privatecoach.app.core.model.Workout

data class WorkoutDetailUiState(
    val workout: Workout? = null,
    val isLoading: Boolean = true,
    val showDeleteDialog: Boolean = false,
    val deleted: Boolean = false,
    val saveTemplateComplete: Boolean = false
)
