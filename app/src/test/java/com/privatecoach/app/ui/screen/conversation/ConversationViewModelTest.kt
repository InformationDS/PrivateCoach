package com.privatecoach.app.ui.screen.conversation

import android.content.Context
import android.net.ConnectivityManager
import app.cash.turbine.test
import com.privatecoach.app.MainDispatcherRule
import com.privatecoach.app.core.ai.AiApiService
import com.privatecoach.app.core.ai.IntentRouter
import com.privatecoach.app.core.ai.SessionContextBuilder
import com.privatecoach.app.core.audio.AudioRecorder
import com.privatecoach.app.core.model.ConversationUiEvent
import com.privatecoach.app.core.model.QuickActionAction
import com.privatecoach.app.core.model.QuickActionChip
import com.privatecoach.app.core.model.SessionContext
import com.privatecoach.app.core.query.QueryEngine
import com.privatecoach.app.domain.repository.SettingsRepository
import com.privatecoach.app.domain.repository.WorkoutRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ConversationViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun `calendar quick action emits navigation event without AI`() = runTest {
        val settings = mockk<SettingsRepository> { every { apiKey } returns flowOf("") }
        val sessionBuilder = mockk<SessionContextBuilder>()
        coEvery { sessionBuilder.build() } returns SessionContext(0, null, emptySet(), emptyList(), emptyList(), emptyList(), 0.0, 0)
        val context = mockk<Context> { every { getSystemService(ConnectivityManager::class.java) } returns null }
        val viewModel = ConversationViewModel(
            mockk<WorkoutRepository>(), mockk<AiApiService>(), mockk<IntentRouter>(), mockk<QueryEngine>(),
            sessionBuilder, settings, mockk<AudioRecorder>(relaxed = true), context
        )

        viewModel.uiEvents.test {
            viewModel.onQuickActionClick(QuickActionChip("日历", "", "", action = QuickActionAction.OPEN_CALENDAR))
            assert(awaitItem() == ConversationUiEvent.OpenCalendar)
        }
    }
}
