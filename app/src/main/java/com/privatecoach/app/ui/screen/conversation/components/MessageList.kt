package com.privatecoach.app.ui.screen.conversation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.privatecoach.app.core.model.AiParsedResult
import com.privatecoach.app.core.model.Message
import com.privatecoach.app.core.model.PendingConfirmData
import com.privatecoach.app.ui.screen.conversation.components.bubbles.AdviceCard
import com.privatecoach.app.ui.screen.conversation.components.bubbles.AiTextBubble
import com.privatecoach.app.ui.screen.conversation.components.bubbles.ChartCard
import com.privatecoach.app.ui.screen.conversation.components.bubbles.ConfirmCard
import com.privatecoach.app.ui.screen.conversation.components.bubbles.DataCard
import com.privatecoach.app.ui.screen.conversation.components.bubbles.SummaryCard
import com.privatecoach.app.ui.screen.conversation.components.bubbles.UserTextBubble
import com.privatecoach.app.ui.screen.conversation.components.bubbles.UserVoiceBubble
import com.privatecoach.app.ui.theme.PcTextSecondary

@Composable
fun MessageList(
    messages: List<Message>,
    pendingConfirm: PendingConfirmData?,
    onConfirm: (AiParsedResult) -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onAppend: () -> Unit,
    onOverwrite: () -> Unit,
    onExerciseChange: (Int, com.privatecoach.app.core.model.ParsedExercise) -> Unit = { _, _ -> },
    onRemoveExercise: (Int) -> Unit = {},
    onAddExercise: () -> Unit = {},
    onFeelingChange: (com.privatecoach.app.core.model.Feeling) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, pendingConfirm) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        items(messages, key = { it.id }) { message ->
            when (message) {
                is Message.UserText -> UserTextBubble(message)
                is Message.UserVoice -> UserVoiceBubble(message)
                is Message.AiText -> AiTextBubble(message)
                is Message.ChartCard -> ChartCard(message)
                is Message.DataCard -> DataCard(message)
                is Message.AdviceCard -> AdviceCard(message)
                is Message.SummaryCard -> SummaryCard(message)
                is Message.ConfirmCard -> {
                    // ConfirmCard in message list (for history, not interactive)
                    // Interactive ConfirmCard is shown via pendingConfirm
                    ConfirmCard(
                        parsedResult = message.parsedResult,
                        sourceText = message.sourceText,
                        isAppendMode = message.isAppendMode,
                        onConfirm = onConfirm,
                        onEdit = onEdit,
                        onCancel = onCancel,
                        onAppend = onAppend,
                        onOverwrite = onOverwrite,
                        onExerciseChange = onExerciseChange,
                        onRemoveExercise = onRemoveExercise,
                        onAddExercise = onAddExercise,
                        onFeelingChange = onFeelingChange
                    )
                }
                is Message.SystemMsg -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = message.text,
                            color = PcTextSecondary,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Show pending confirm card at the bottom (non-persisted, interactive)
        if (pendingConfirm != null) {
            item(key = "pending_confirm") {
                ConfirmCard(
                    parsedResult = pendingConfirm.parsedResult,
                    sourceText = pendingConfirm.sourceText,
                    isAppendMode = pendingConfirm.isAppendMode,
                    onConfirm = onConfirm,
                    onEdit = onEdit,
                    onCancel = onCancel,
                    onAppend = onAppend,
                    onOverwrite = onOverwrite,
                    onExerciseChange = onExerciseChange,
                    onRemoveExercise = onRemoveExercise,
                    onAddExercise = onAddExercise,
                    onFeelingChange = onFeelingChange
                )
            }
        }

        // Bottom spacer for comfortable scroll
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
