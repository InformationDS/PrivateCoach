package com.privatecoach.app.ui.screen.conversation.components.bubbles

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.privatecoach.app.core.model.Message
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcSurface
import com.privatecoach.app.ui.theme.PcTextPrimary

@Composable
fun UserTextBubble(message: Message.UserText) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(1.dp, PcDivider, PcShapes.small)
                .padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = message.content,
                color = PcTextPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
