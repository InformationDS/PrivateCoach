package com.privatecoach.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_API_ENDPOINT = stringPreferencesKey("api_endpoint")
        private val KEY_API_KEY = stringPreferencesKey("api_key")
        private val KEY_MODEL_NAME = stringPreferencesKey("model_name")

        const val DEFAULT_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models"
        const val DEFAULT_MODEL = "gemini-2.0-flash"
    }

    val apiEndpoint: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_API_ENDPOINT] ?: DEFAULT_ENDPOINT
    }

    val apiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_API_KEY] ?: ""
    }

    val modelName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_MODEL_NAME] ?: DEFAULT_MODEL
    }

    suspend fun setApiEndpoint(endpoint: String) {
        context.dataStore.edit { it[KEY_API_ENDPOINT] = endpoint }
    }

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { it[KEY_API_KEY] = key }
    }

    suspend fun setModelName(name: String) {
        context.dataStore.edit { it[KEY_MODEL_NAME] = name }
    }
}
