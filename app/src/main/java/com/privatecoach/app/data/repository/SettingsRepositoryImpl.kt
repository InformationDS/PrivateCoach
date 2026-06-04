package com.privatecoach.app.data.repository

import com.privatecoach.app.data.datastore.SettingsDataStore
import com.privatecoach.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    override val apiEndpoint: Flow<String> = dataStore.apiEndpoint
    override val apiKey: Flow<String> = dataStore.apiKey
    override val modelName: Flow<String> = dataStore.modelName

    override suspend fun setApiEndpoint(endpoint: String) = dataStore.setApiEndpoint(endpoint)
    override suspend fun setApiKey(key: String) = dataStore.setApiKey(key)
    override suspend fun setModelName(name: String) = dataStore.setModelName(name)
}
