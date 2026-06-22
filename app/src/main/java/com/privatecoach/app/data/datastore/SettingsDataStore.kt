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
import com.privatecoach.app.core.security.KeyStoreManager
import android.util.Base64
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyStoreManager: KeyStoreManager
) {
    companion object {
        private val KEY_API_ENDPOINT = stringPreferencesKey("api_endpoint")
        private val KEY_API_KEY = stringPreferencesKey("api_key")
        private val KEY_API_KEY_IV = stringPreferencesKey("api_key_iv")
        private val KEY_API_KEY_CIPHERTEXT = stringPreferencesKey("api_key_ciphertext")
        private val KEY_MODEL_NAME = stringPreferencesKey("model_name")

        const val DEFAULT_ENDPOINT = "https://api.xiaomimimo.com/v1"
        const val DEFAULT_MODEL = "mimo-v2.5-pro"
    }

    val apiEndpoint: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_API_ENDPOINT] ?: DEFAULT_ENDPOINT
    }

    val apiKey: Flow<String> = context.dataStore.data.map { prefs ->
        val iv = prefs[KEY_API_KEY_IV]
        val ciphertext = prefs[KEY_API_KEY_CIPHERTEXT]
        if (iv != null && ciphertext != null) {
            runCatching {
                keyStoreManager.decrypt(
                    Base64.decode(iv, Base64.NO_WRAP),
                    Base64.decode(ciphertext, Base64.NO_WRAP)
                )
            }.getOrDefault("")
        } else {
            prefs[KEY_API_KEY] ?: ""
        }
    }

    val modelName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_MODEL_NAME] ?: DEFAULT_MODEL
    }

    suspend fun setApiEndpoint(endpoint: String) {
        context.dataStore.edit { it[KEY_API_ENDPOINT] = endpoint }
    }

    suspend fun setApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_API_KEY)
            if (key.isBlank()) {
                prefs.remove(KEY_API_KEY_IV)
                prefs.remove(KEY_API_KEY_CIPHERTEXT)
            } else {
                val (iv, encrypted) = keyStoreManager.encrypt(key)
                prefs[KEY_API_KEY_IV] = Base64.encodeToString(iv, Base64.NO_WRAP)
                prefs[KEY_API_KEY_CIPHERTEXT] = Base64.encodeToString(encrypted, Base64.NO_WRAP)
            }
        }
    }

    suspend fun setModelName(name: String) {
        context.dataStore.edit { it[KEY_MODEL_NAME] = name }
    }

    suspend fun migrateLegacyApiKey() {
        val legacy = context.dataStore.data.first()[KEY_API_KEY] ?: return
        setApiKey(legacy)
    }
}
