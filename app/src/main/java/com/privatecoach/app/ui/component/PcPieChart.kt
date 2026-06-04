package com.privatecoach.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcAccentTeal
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcSpacing
import com.privatecoach.app.ui.theme.PcTextPrimary
import com.privatecoach.app.ui.theme.PcTextSecondary
import kotlin.math.cos
import kotlin.math.sin

data class ChartSegment(
    val label: String,
    val value: Float,
    val color: Color
)

val BodyPartColors = mapOf(
    "胸部" to PcAccentCopper,
    "背部" to PcAccentTeal,
    "腿部" to Color(0xFF86EFAC),
    "肩部" to Color(0xFFFCA5A5),
    "二头肌" to Color(0xFFA78BFA),
    "三头肌" to Color(0xFFC084FC),
    "核心" to Color(0xFFFDE68A),
    "全身" to PcTextPrimary
)

fun getBodyPartColor(bodyPart: String): Color {
    return BodyPartColors.entries.firstOrNull { bodyPart.contains(it.key) }?.value
        ?: PcAccentCopper
}

@Composable
fun PcPieChart(
    segments: List<ChartSegment>,
    modifier: Modifier = Modifier,
    donutHoleFraction: Float = 0.35f,
    centerText: String? = null,
    showLegend: Boolean = true
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = PcTextSecondary, fontWeight = FontWeight.Normal)
    val centerLabelStyle = TextStyle(fontSize = 14.sp, color = PcTextPrimary, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)

    val total = segments.sumOf { it.value.toDouble() }.toFloat()
    val chartSize = 200.dp
    val strokeWidth = 1.dp

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .size(chartSize)
                .padding(8.dp)
        ) {
            if (segments.isEmpty()) return@Canvas

            val center = Offset(size.width / 2, size.height / 2)
            val outerRadius = minOf(size.width, size.height) / 2 * 0.85f
            val innerRadius = outerRadius * donutHoleFraction

            var startAngle = -90f

            segments.forEach { segment ->
                val sweepAngle = if (total > 0) (segment.value / total) * 360f else 0f

                // Draw segment
                drawArc(
                    color = segment.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = Size(outerRadius * 2, outerRadius * 2)
                )

                // Draw percentage label on segment
                val percentage = if (total > 0) segment.value / total else 0f
                if (percentage > 0.05f && sweepAngle > 15f) {
                    val midAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                    val labelRadius = outerRadius * 0.7f
                    val labelX = center.x + labelRadius * cos(midAngle).toFloat()
                    val labelY = center.y + labelRadius * sin(midAngle).toFloat()
                    val pctLabel = "${(percentage * 100).toInt()}%"
                    val labelResult = textMeasurer.measure(pctLabel, labelStyle)
                    // Only draw if it fits
                    if (labelResult.size.width < outerRadius * 0.5f) {
                        drawText(
                            textLayoutResult = labelResult,
                            topLeft = Offset(
                                labelX - labelResult.size.width / 2,
                                labelY - labelResult.size.height / 2
                            )
                        )
                    }
                }

                // Segment border
                if (sweepAngle > 0f) {
                    drawArc(
                        color = PcDivider,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                        size = Size(outerRadius * 2, outerRadius * 2),
                        style = Stroke(width = strokeWidth.toPx())
                    )
                }

                startAngle += sweepAngle
            }

            // Donut hole (draw a circle in background color to create the hole)
            if (donutHoleFraction > 0f) {
                drawCircle(
                    color = Color(0xFF0F172A), // PcBackground
                    radius = innerRadius,
                    center = center
                )
                // Inner border
                drawCircle(
                    color = PcDivider,
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Center text
            if (centerText != null) {
                val ctResult = textMeasurer.measure(centerText, centerLabelStyle)
                drawText(
                    textLayoutResult = ctResult,
                    topLeft = Offset(
                        center.x - ctResult.size.width / 2,
                        center.y - ctResult.size.height / 2
                    )
                )
            }
        }

        // Legend
        if (showLegend && segments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(PcSpacing.sm))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = PcSpacing.md),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                segments.forEach { segment ->
                    val percentage = if (total > 0) "%.0f%%".format(segment.value / total * 100) else "0%"
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.size(8.dp)) {
                            drawCircle(color = segment.color, radius = 4.dp.toPx())
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = segment.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = PcTextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = percentage,
                            style = MaterialTheme.typography.labelSmall,
                            color = PcTextSecondary
                        )
                    }
                }
            }
        }
    }
}
