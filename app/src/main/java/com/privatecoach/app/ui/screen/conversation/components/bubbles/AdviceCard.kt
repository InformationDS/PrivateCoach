package com.privatecoach.app.ui.screen.conversation.components.bubbles

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatecoach.app.core.model.DataSufficiency
import com.privatecoach.app.core.model.Message
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcAccentTeal
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcTextPrimary
import com.privatecoach.app.ui.theme.PcTextSecondary

@Composable
fun AdviceCard(
    message: Message.AdviceCard,
    onHelpful: (() -> Unit)? = null,
    onNotHelpful: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(1.dp, PcAccentTeal, PcShapes.small)
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "💡 训练建议",
            color = PcAccentTeal,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = PcDivider, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Data sufficiency warning
        if (message.result.dataSufficiency != DataSufficiency.SUFFICIENT) {
            Text(
                text = when (message.result.dataSufficiency) {
                    DataSufficiency.LIMITED -> "⚠️ 训练数据有限，以下分析仅供参考。"
                    DataSufficiency.INSUFFICIENT -> "⚠️ 训练数据不足，以下是通用训练建议。"
                    else -> ""
                },
                color = PcTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Data summary
        Text(
            text = "📊 数据概况",
            color = PcAccentCopper,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message.result.dataSummary,
            color = PcTextPrimary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Analysis
        Text(
            text = "🔍 分析",
            color = PcAccentCopper,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message.result.analysis,
            color = PcTextPrimary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Suggestions
        Text(
            text = "💪 建议",
            color = PcAccentCopper,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        message.result.suggestions.forEachIndexed { index, suggestion ->
            Text(
                text = "${index + 1}. $suggestion",
                color = PcTextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Feedback buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onHelpful?.invoke() }) {
                Text("👍 有帮助", color = PcTextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = { onNotHelpful?.invoke() }) {
                Text("👎 不相关", color = PcTextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
