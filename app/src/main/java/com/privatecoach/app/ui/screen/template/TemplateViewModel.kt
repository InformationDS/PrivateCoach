package com.privatecoach.app.ui.screen.template

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecoach.app.core.model.BodyPart
import com.privatecoach.app.core.model.TemplateExercise
import com.privatecoach.app.core.model.TrainingTemplate
import com.privatecoach.app.core.model.WorkoutType
import com.privatecoach.app.domain.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplateListViewModel @Inject constructor(
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplateListUiState())
    val uiState: StateFlow<TemplateListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            templateRepository.getAllTemplates().collect { templates ->
                _uiState.update { it.copy(templates = templates, isLoading = false) }
            }
        }
    }

    fun deleteTemplate(id: Long) {
        viewModelScope.launch { templateRepository.deleteTemplate(id) }
    }
}

@HiltViewModel
class TemplateEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val templateId: Long? = savedStateHandle.get<Long>("templateId")

    private val _uiState = MutableStateFlow(TemplateEditUiState())
    val uiState: StateFlow<TemplateEditUiState> = _uiState.asStateFlow()

    init {
        if (templateId != null && templateId > 0) {
            viewModelScope.launch {
                templateRepository.getTemplateById(templateId)?.let { tmpl ->
                    _uiState.update {
                        it.copy(
                            name = tmpl.name,
                            type = tmpl.type,
                            bodyPart = tmpl.bodyPart,
                            exercises = tmpl.exercises.ifEmpty { listOf(TemplateExercise(name = "", weightUnit = "kg")) }
                        )
                    }
                }
            }
        }
    }

    fun setName(name: String) = _uiState.update { it.copy(name = name) }
    fun setType(type: WorkoutType) = _uiState.update { it.copy(type = type) }
    fun setBodyPart(part: BodyPart?) = _uiState.update { it.copy(bodyPart = part) }

    fun updateExerciseName(index: Int, name: String) {
        _uiState.update { state ->
            val exs = state.exercises.toMutableList()
            if (index < exs.size) exs[index] = exs[index].copy(name = name)
            state.copy(exercises = exs)
        }
    }

    fun updateExerciseUnit(index: Int, unit: String) {
        _uiState.update { state ->
            val exs = state.exercises.toMutableList()
            if (index < exs.size) exs[index] = exs[index].copy(weightUnit = unit)
            state.copy(exercises = exs)
        }
    }

    fun addExercise() {
        _uiState.update { state ->
            state.copy(exercises = state.exercises + TemplateExercise(name = "", weightUnit = "kg"))
        }
    }

    fun removeExercise(index: Int) {
        _uiState.update { state ->
            if (state.exercises.size <= 1) state
            else state.copy(exercises = state.exercises.toMutableList().also { it.removeAt(index) })
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank() || state.exercises.all { it.name.isBlank() }) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val template = TrainingTemplate(
                id = templateId ?: 0,
                name = state.name,
                type = state.type,
                bodyPart = state.bodyPart,
                exercises = state.exercises.filter { it.name.isNotBlank() }
            )
            if (templateId != null && templateId > 0) {
                templateRepository.updateTemplate(template)
            } else {
                templateRepository.saveTemplate(template)
            }
            _uiState.update { it.copy(isSaving = false, saveComplete = true) }
        }
    }
}
