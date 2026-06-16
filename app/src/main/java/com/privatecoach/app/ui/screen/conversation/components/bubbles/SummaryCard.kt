package com.privatecoach.app.ui.screen.conversation.components.bubbles

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatecoach.app.core.model.Message
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcTextPrimary
import com.privatecoach.app.ui.theme.PcTextSecondary

@Composable
fun SummaryCard(message: Message.SummaryCard) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.dp, PcDivider, PcShapes.small)
            .padding(16.dp)
    ) {
        Text(
            text = "📊 训练总结",
            color = PcAccentCopper,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = PcDivider, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Overview
        Text(
            text = message.result.overview,
            color = PcTextPrimary,
            style = MaterialTheme.typography.bodyMedium
        )

        // Highlights
        if (message.result.highlights.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "✨ 亮点",
                color = PcAccentCopper,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            message.result.highlights.forEach { highlight ->
                Text(
                    text = "· $highlight",
                    color = PcTextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        // Concerns
        if (message.result.concerns.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "⚠️ 需关注",
                color = PcAccentCopper,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            message.result.concerns.forEach { concern ->
                Text(
                    text = "· $concern",
                    color = PcTextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }

        // Comparison
        if (message.result.comparisonText.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "📈 ${message.result.comparisonText}",
                color = PcTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
