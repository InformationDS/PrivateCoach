package com.privatecoach.app.ui.screen.conversation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privatecoach.app.core.model.ConversationState
import com.privatecoach.app.ui.screen.conversation.components.ChatInputBar
import com.privatecoach.app.ui.screen.conversation.components.MessageList
import com.privatecoach.app.ui.screen.conversation.components.QuickActionChips
import com.privatecoach.app.ui.screen.conversation.components.VoiceRecordButton
import com.privatecoach.app.ui.screen.conversation.components.WelcomeHeader
import com.privatecoach.app.ui.theme.PcBackground
import com.privatecoach.app.ui.theme.PcTextPrimary
import com.privatecoach.app.ui.theme.PcTextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToManualEntry: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val state by viewModel.state.collectAsState()
    val pendingConfirm by viewModel.pendingConfirm.collectAsState()
    val sessionContext by viewModel.sessionContext.collectAsState()
    val quickActions by viewModel.quickActions.collectAsState()
    val aiAvailable by viewModel.aiAvailable.collectAsState()
    val inputText by viewModel.inputText.collectAsState()

    val todayLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日 EEEE"))

    Scaffold(
        containerColor = PcBackground,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI助手",
                            color = PcTextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = todayLabel,
                            color = PcTextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onOpenDrawer) {
                        Text(
                            text = "☰",
                            color = PcTextPrimary,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PcBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // AI availability banner
            if (!aiAvailable) {
                androidx.compose.material3.Surface(
                    color = com.privatecoach.app.ui.theme.PcDivider,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⚠️ AI 暂不可用（未配置 API Key 或网络断开）。可在设置中配置，或使用手动录入。",
                        color = PcTextSecondary,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Welcome header (only show at top when few messages)
            if (messages.isEmpty() || (messages.size <= 2 && messages.all {
                    it is com.privatecoach.app.core.model.Message.SystemMsg ||
                        it is com.privatecoach.app.core.model.Message.AiText
                })) {
                WelcomeHeader(sessionContext = sessionContext)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Quick action chips
            if (quickActions.isNotEmpty()) {
                QuickActionChips(
                    chips = quickActions,
                    onChipClick = { viewModel.onQuickActionClick(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Message list (weight 1f to fill remaining space)
            MessageList(
                messages = messages,
                pendingConfirm = pendingConfirm,
                onConfirm = { viewModel.confirmWorkout(it) },
                onEdit = { /* navigate to manual entry */ },
                onCancel = { viewModel.cancelWorkout() },
                onAppend = {
                    // User chose "append" — just confirm with merge
                    pendingConfirm?.let { viewModel.confirmWorkout(it.parsedResult) }
                },
                onOverwrite = {
                    // User chose "overwrite" — handled by existing WorkoutRepository merge behavior
                    pendingConfirm?.let { viewModel.confirmWorkout(it.parsedResult) }
                },
                modifier = Modifier.weight(1f)
            )

            // Input area
            VoiceRecordButton(
                isRecording = state == ConversationState.RECORDING,
                onStartRecording = { viewModel.onStartRecording() },
                onStopRecording = { /* handled by AudioRecorder integration */ },
                onCancelRecording = { viewModel.onCancelRecording() },
                enabled = aiAvailable
            )

            ChatInputBar(
                inputText = inputText,
                onInputChange = { viewModel.onInputChange(it) },
                onSend = { viewModel.onSend() },
                onAttachmentClick = { /* show template picker */ },
                enabled = aiAvailable
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
