package com.privatecoach.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.privatecoach.app.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PrivateCoachApplication : Application() {
    @Inject lateinit var settingsRepository: SettingsRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch { settingsRepository.migrateLegacyApiKey() }
    }
}
