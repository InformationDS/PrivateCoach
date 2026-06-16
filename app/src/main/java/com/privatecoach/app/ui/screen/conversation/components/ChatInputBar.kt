package com.privatecoach.app.ui.screen.conversation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcBackground
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcTextPrimary
import com.privatecoach.app.ui.theme.PcTextSecondary

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachmentClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PcDivider, PcShapes.small)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Attachment button
        IconButton(
            onClick = onAttachmentClick,
            modifier = Modifier,
            enabled = enabled
        ) {
            Text(
                text = "📎",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Text input
        BasicTextField(
            value = inputText,
            onValueChange = onInputChange,
            enabled = enabled,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = PcTextPrimary),
            decorationBox = { innerTextField ->
                if (inputText.isEmpty()) {
                    Text(
                        text = if (enabled) "输入消息或训练内容..." else "AI 暂不可用",
                        color = PcTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                innerTextField()
            }
        )

        // Send button (only visible when text is not empty)
        if (inputText.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onSend) {
                Text(
                    text = "📤",
                    color = PcAccentCopper,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
