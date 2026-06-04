package com.privatecoach.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcAccentTeal
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcSpacing
import com.privatecoach.app.ui.theme.PcTextSecondary

data class ChartLine(
    val label: String,
    val values: List<Double>,
    val color: Color
)

val ChartLineColors = listOf(
    PcAccentCopper,
    PcAccentTeal,
    Color(0xFF86EFAC),
    Color(0xFFFCA5A5),
    Color(0xFFA78BFA)
)

@Composable
fun PcLineChart(
    lines: List<ChartLine>,
    xAxisLabels: List<String>,
    modifier: Modifier = Modifier,
    showLegend: Boolean = true,
    showGrid: Boolean = true,
    formatValue: (Double) -> String = { "%.0f".format(it) }
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = PcTextSecondary, fontWeight = FontWeight.Normal)

    val allValues = lines.flatMap { it.values }
    val minY = allValues.minOrNull() ?: 0.0
    val maxY = allValues.maxOrNull() ?: 1.0
    val yRange = if (maxY - minY < 0.001) 1.0 else maxY - minY
    val yMin = minY - yRange * 0.1
    val yMax = maxY + yRange * 0.1
    val dataRange = yMax - yMin

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val leftPadding = 44.dp.toPx()
            val rightPadding = 16.dp.toPx()
            val topPadding = 20.dp.toPx()
            val bottomPadding = 32.dp.toPx()

            val drawWidth = size.width - leftPadding - rightPadding
            val drawHeight = size.height - topPadding - bottomPadding

            // Grid lines and Y-axis labels
            val gridCount = 5
            for (i in 0..gridCount) {
                val fraction = i.toFloat() / gridCount
                val y = topPadding + (1 - fraction) * drawHeight
                val value = yMin + fraction * dataRange

                if (showGrid && i > 0) {
                    drawLine(
                        color = PcDivider,
                        start = Offset(leftPadding, y),
                        end = Offset(size.width - rightPadding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val label = formatValue(value)
                val textResult = textMeasurer.measure(label, labelStyle)
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        leftPadding - textResult.size.width - 6.dp.toPx(),
                        y - textResult.size.height / 2
                    )
                )
            }

            // X-axis labels
            if (xAxisLabels.isNotEmpty()) {
                val maxPoints = lines.maxOfOrNull { it.values.size } ?: xAxisLabels.size
                xAxisLabels.take(maxPoints).forEachIndexed { index, label ->
                    val x = if (maxPoints > 1) {
                        leftPadding + (index.toFloat() / (maxPoints - 1)) * drawWidth
                    } else {
                        leftPadding + drawWidth / 2
                    }
                    val textResult = textMeasurer.measure(label, labelStyle)
                    drawText(
                        textLayoutResult = textResult,
                        topLeft = Offset(
                            x - textResult.size.width / 2,
                            size.height - bottomPadding + 6.dp.toPx()
                        )
                    )
                }
            }

            // Draw lines
            lines.forEach { line ->
                val color = line.color
                if (line.values.size < 2) return@forEach

                val path = Path()
                line.values.forEachIndexed { i, value ->
                    val x = leftPadding + (i.toFloat() / (line.values.size - 1)) * drawWidth
                    val y = topPadding + ((yMax - value) / dataRange).toFloat() * drawHeight

                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Data dots
                line.values.forEachIndexed { i, value ->
                    val x = leftPadding + (i.toFloat() / (line.values.size - 1)) * drawWidth
                    val y = topPadding + ((yMax - value) / dataRange).toFloat() * drawHeight
                    drawCircle(
                        color = color,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // Legend
        if (showLegend && lines.isNotEmpty()) {
            Spacer(modifier = Modifier.height(PcSpacing.sm))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = PcSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                lines.forEach { line ->
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = line.color, radius = 5.dp.toPx())
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = line.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = PcTextSecondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }
    }
}
