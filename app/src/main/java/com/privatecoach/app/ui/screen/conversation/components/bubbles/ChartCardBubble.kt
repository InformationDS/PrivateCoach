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
import com.privatecoach.app.core.model.ChartData
import com.privatecoach.app.ui.component.ChartBar
import com.privatecoach.app.ui.component.ChartLine
import com.privatecoach.app.ui.component.ChartSegment
import com.privatecoach.app.ui.component.PcBarChart
import com.privatecoach.app.ui.component.PcLineChart
import com.privatecoach.app.ui.component.PcPieChart
import com.privatecoach.app.ui.component.ChartLineColors
import com.privatecoach.app.ui.component.getBodyPartColor
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

        when (val data = message.chartData) {
            is ChartData.Line -> PcLineChart(
                lines = listOf(ChartLine(data.seriesLabel, data.values, ChartLineColors.first())),
                xAxisLabels = data.labels,
                showLegend = false
            )
            is ChartData.Bars -> PcBarChart(data.labels.zip(data.values).map { ChartBar(it.first, it.second) })
            is ChartData.Pie -> PcPieChart(data.labels.zip(data.values).map {
                ChartSegment(it.first, it.second, getBodyPartColor(it.first))
            })
            null -> Text("暂无图表数据", color = PcTextSecondary, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message.interpretation,
            color = PcTextPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
