package com.privatecoach.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcSpacing
import com.privatecoach.app.ui.theme.PcTextSecondary

data class ChartBar(
    val label: String,
    val value: Float,
    val color: Color = PcAccentCopper
)

@Composable
fun PcBarChart(
    bars: List<ChartBar>,
    modifier: Modifier = Modifier,
    showValues: Boolean = true,
    valueLabelTransform: (Float) -> String = { "%.0f".format(it) }
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = PcTextSecondary, fontWeight = FontWeight.Normal)
    val valueStyle = TextStyle(fontSize = 10.sp, color = PcTextSecondary, fontWeight = FontWeight.Medium)

    val maxValue = bars.maxOfOrNull { it.value } ?: 1f
    val yMax = maxValue * 1.15f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (bars.isEmpty()) return@Canvas

        val leftPadding = 16.dp.toPx()
        val rightPadding = 16.dp.toPx()
        val topPadding = 24.dp.toPx()
        val bottomPadding = 32.dp.toPx()

        val drawWidth = size.width - leftPadding - rightPadding
        val drawHeight = size.height - topPadding - bottomPadding

        val slotWidth = drawWidth / bars.size
        val barWidth = slotWidth * 0.6f
        val barCornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())

        bars.forEachIndexed { index, bar ->
            val barHeight = ((bar.value / yMax) * drawHeight).coerceAtLeast(1.dp.toPx())
            val barLeft = leftPadding + index * slotWidth + (slotWidth - barWidth) / 2
            val barTop = topPadding + drawHeight - barHeight

            // Draw bar
            drawRoundRect(
                color = bar.color,
                topLeft = Offset(barLeft, barTop),
                size = Size(barWidth, barHeight),
                cornerRadius = barCornerRadius
            )

            // Value label above bar
            if (showValues) {
                val valueLabel = valueLabelTransform(bar.value)
                val textResult = textMeasurer.measure(valueLabel, valueStyle)
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        barLeft + barWidth / 2 - textResult.size.width / 2,
                        barTop - textResult.size.height - 4.dp.toPx()
                    )
                )
            }

            // X-axis label below bar
            val labelTextResult = textMeasurer.measure(bar.label, labelStyle)
            drawText(
                textLayoutResult = labelTextResult,
                topLeft = Offset(
                    barLeft + barWidth / 2 - labelTextResult.size.width / 2,
                    size.height - bottomPadding + 6.dp.toPx()
                )
            )
        }
    }
}
