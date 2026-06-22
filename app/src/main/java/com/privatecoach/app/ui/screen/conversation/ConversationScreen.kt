package com.privatecoach.app.ui.screen.conversation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privatecoach.app.core.model.ConversationState
import com.privatecoach.app.core.model.AiAvailability
import com.privatecoach.app.core.model.ConversationUiEvent
import com.privatecoach.app.core.model.SameDayWriteMode
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
    val aiAvailability by viewModel.aiAvailability.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val context = LocalContext.current
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.onStartRecording() else viewModel.onCancelRecording() }

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                ConversationUiEvent.OpenDashboard -> onNavigateToDashboard()
                ConversationUiEvent.OpenCalendar -> onNavigateToCalendar()
                ConversationUiEvent.OpenSettings -> onNavigateToSettings()
                ConversationUiEvent.OpenManualEntry -> onNavigateToManualEntry()
            }
        }
    }

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
            if (aiAvailability != AiAvailability.READY) {
                val bannerText = when (aiAvailability) {
                    AiAvailability.NO_KEY -> "⚠️ AI 尚未完成配置。请检查 API Endpoint、模型名和 API Key。"
                    AiAvailability.OFFLINE -> "⚠️ 当前网络不可用。文本本地查询仍可使用，远程 AI 能力暂不可用。"
                    AiAvailability.API_ERROR -> "⚠️ AI 接口调用失败。请检查 Endpoint、模型名、API Key 是否与服务商文档一致。"
                    AiAvailability.READY -> ""
                }
                androidx.compose.material3.Surface(
                    color = com.privatecoach.app.ui.theme.PcDivider,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = bannerText,
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
                onConfirm = { viewModel.confirmWorkout() },
                onEdit = { /* navigate to manual entry */ },
                onCancel = { viewModel.cancelWorkout() },
                onAppend = {
                    // User chose "append" — just confirm with merge
                    viewModel.confirmWorkout(SameDayWriteMode.APPEND)
                },
                onOverwrite = {
                    // User chose "overwrite" — handled by existing WorkoutRepository merge behavior
                    viewModel.confirmWorkout(SameDayWriteMode.OVERWRITE)
                },
                onExerciseChange = viewModel::updateDraftExercise,
                onRemoveExercise = viewModel::removeDraftExercise,
                onAddExercise = viewModel::addDraftExercise,
                onFeelingChange = viewModel::updateDraftFeeling,
                modifier = Modifier.weight(1f)
            )

            // Input area
            VoiceRecordButton(
                isRecording = state == ConversationState.RECORDING,
                onStartRecording = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        viewModel.onStartRecording()
                    } else {
                        recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopRecording = { viewModel.onStopRecording() },
                onCancelRecording = { viewModel.onCancelRecording() },
                enabled = aiAvailability == AiAvailability.READY
            )

            ChatInputBar(
                inputText = inputText,
                onInputChange = { viewModel.onInputChange(it) },
                onSend = { viewModel.onSend() },
                onAttachmentClick = onNavigateToManualEntry,
                enabled = true
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
