package com.privatecoach.app.ui.screen.settings

import com.privatecoach.app.core.model.ExportContainer
import com.privatecoach.app.core.model.ImportResult

data class SettingsUiState(
    val apiEndpoint: String = "",
    val apiKey: String = "",
    val modelName: String = "",
    val apiKeyVisible: Boolean = false,
    val isSaving: Boolean = false,
    val saveMessage: String? = null,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportContainer: ExportContainer? = null,
    val exportResult: String? = null,
    val importResult: ImportResult? = null
)
