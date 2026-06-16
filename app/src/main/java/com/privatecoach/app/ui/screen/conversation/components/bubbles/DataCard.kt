package com.privatecoach.app.ui.screen.conversation.components.bubbles

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatecoach.app.core.model.Message
import com.privatecoach.app.core.model.TrendDirection
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcTextPrimary
import com.privatecoach.app.ui.theme.PcTextSecondary

@Composable
fun DataCard(message: Message.DataCard) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.dp, PcDivider, PcShapes.small)
            .padding(16.dp)
    ) {
        Text(
            text = message.title,
            color = PcAccentCopper,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = PcDivider, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        message.stats.forEach { stat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.label,
                    color = PcTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stat.value,
                    color = if (stat.isHighlighted) PcAccentCopper else PcTextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (stat.isHighlighted) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}
