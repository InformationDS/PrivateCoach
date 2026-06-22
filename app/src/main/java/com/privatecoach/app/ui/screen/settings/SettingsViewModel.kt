package com.privatecoach.app.ui.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.privatecoach.app.core.export.DataExporter
import com.privatecoach.app.core.export.DataImporter
import com.privatecoach.app.core.model.ImportResult
import com.privatecoach.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dataExporter: DataExporter,
    private val dataImporter: DataImporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.apiEndpoint.collect { endpoint ->
                _uiState.update { it.copy(apiEndpoint = endpoint) }
            }
        }
        viewModelScope.launch {
            settingsRepository.apiKey.collect { key ->
                _uiState.update { it.copy(apiKey = key) }
            }
        }
        viewModelScope.launch {
            settingsRepository.modelName.collect { model ->
                _uiState.update { it.copy(modelName = model) }
            }
        }
    }

    fun setApiEndpoint(endpoint: String) = _uiState.update { it.copy(apiEndpoint = endpoint) }
    fun setApiKey(key: String) = _uiState.update { it.copy(apiKey = key) }
    fun setModelName(name: String) = _uiState.update { it.copy(modelName = name) }
    fun toggleApiKeyVisibility() = _uiState.update { it.copy(apiKeyVisible = !it.apiKeyVisible) }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                val state = _uiState.value
                settingsRepository.setApiEndpoint(state.apiEndpoint)
                settingsRepository.setApiKey(state.apiKey)
                settingsRepository.setModelName(state.modelName.trim())
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, saveMessage = "设置已保存") }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, saveMessage = error.message ?: "设置保存失败") }
            }
        }
    }

    fun prepareExport() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isExporting = true, exportResult = null) }
            try {
                val container = dataExporter.exportToJson()
                _uiState.update { it.copy(exportContainer = container, isExporting = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, exportResult = "导出失败: ${e.message}") }
            }
        }
    }

    fun writeExportToUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val container = _uiState.value.exportContainer ?: return@launch
            try {
                dataExporter.writeToUri(context, uri, container)
                _uiState.update { it.copy(exportContainer = null, exportResult = "导出成功 ✓") }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportContainer = null, exportResult = "写入失败: ${e.message}") }
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isImporting = true, importResult = null) }
            try {
                val container = dataImporter.readFromUri(context, uri)
                val result = dataImporter.importData(container)
                _uiState.update { it.copy(isImporting = false, importResult = result) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importResult = ImportResult(0, 0, listOf("导入失败: ${e.message}"))
                    )
                }
            }
        }
    }

    fun clearResults() {
        _uiState.update { it.copy(exportResult = null, importResult = null) }
    }
}
