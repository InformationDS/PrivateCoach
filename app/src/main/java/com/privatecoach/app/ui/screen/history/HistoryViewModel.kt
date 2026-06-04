package com.privatecoach.app.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecoach.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            workoutRepository.getAllWorkouts().collect { workouts ->
                _uiState.update { it.copy(workouts = workouts, isLoading = false) }
            }
        }
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            if (query.isBlank()) {
                workoutRepository.getAllWorkouts().collect { workouts ->
                    _uiState.update { it.copy(workouts = workouts) }
                }
            } else {
                workoutRepository.searchWorkouts(query).collect { workouts ->
                    _uiState.update { it.copy(workouts = workouts) }
                }
            }
        }
    }
}
