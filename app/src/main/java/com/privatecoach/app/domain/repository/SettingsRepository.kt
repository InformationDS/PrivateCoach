package com.privatecoach.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val apiEndpoint: Flow<String>
    val apiKey: Flow<String>
    val modelName: Flow<String>

    suspend fun setApiEndpoint(endpoint: String)
    suspend fun setApiKey(key: String)
    suspend fun setModelName(name: String)
}
