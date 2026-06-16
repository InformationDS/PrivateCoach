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
import com.privatecoach.app.core.model.ChartType
import com.privatecoach.app.core.model.Message
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcTextPrimary
import com.privatecoach.app.ui.theme.PcTextSecondary

@Composable
fun ChartCard(message: Message.ChartCard) {
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
        Spacer(modifier = Modifier.height(12.dp))

        // Chart placeholder — actual chart rendering happens in ChartCard
        // via the ConversationScreen which renders PcLineChart/PcBarChart/PcPieChart
        // based on chartData passed through state
        Text(
            text = "[图表区域]",
            color = PcTextSecondary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message.interpretation,
            color = PcTextPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
