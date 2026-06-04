package com.privatecoach.app.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Dashboard : Screen
    @Serializable data object Calendar : Screen
    @Serializable data object Analysis : Screen
    @Serializable data object History : Screen
    @Serializable data object Settings : Screen
    // Full-screen destinations
    @Serializable data object Record : Screen
    @Serializable data object ConfirmResult : Screen
    @Serializable data class ManualEntry(val templateId: Long? = null) : Screen
    @Serializable data class WorkoutDetail(val workoutId: Long) : Screen
    @Serializable data object TemplateList : Screen
    @Serializable data class TemplateEdit(val templateId: Long? = null) : Screen
}
