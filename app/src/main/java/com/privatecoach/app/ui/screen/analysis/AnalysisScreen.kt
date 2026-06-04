package com.privatecoach.app.ui.screen.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privatecoach.app.core.model.TimeRange
import com.privatecoach.app.ui.component.ChartBar
import com.privatecoach.app.ui.component.ChartLine
import com.privatecoach.app.ui.component.ChartLineColors
import com.privatecoach.app.ui.component.ChartSegment
import com.privatecoach.app.ui.component.PcBarChart
import com.privatecoach.app.ui.component.PcCard
import com.privatecoach.app.ui.component.PcEmptyState
import com.privatecoach.app.ui.component.PcLineChart
import com.privatecoach.app.ui.component.PcLineDivider
import com.privatecoach.app.ui.component.PcLoadingIndicator
import com.privatecoach.app.ui.component.PcPieChart
import com.privatecoach.app.ui.component.PcStatValue
import com.privatecoach.app.ui.component.PcTag
import com.privatecoach.app.ui.component.getBodyPartColor
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcBackground
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcSpacing
import com.privatecoach.app.ui.theme.PcTextDisabled
import com.privatecoach.app.ui.theme.PcTextSecondary
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = PcBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "数据分析",
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PcBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = PcSpacing.md)
        ) {
            Spacer(modifier = Modifier.height(PcSpacing.sm))

            // Custom tab row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PcSpacing.sm)
            ) {
                AnalysisTab.entries.forEach { tab ->
                    val selected = uiState.selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                1.dp,
                                if (selected) PcAccentCopper else PcDivider,
                                PcShapes.small
                            )
                            .clickable { viewModel.setTab(tab) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            tab.label,
                            color = if (selected) PcAccentCopper else PcTextSecondary,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(PcSpacing.md))
            PcLineDivider()
            Spacer(modifier = Modifier.height(PcSpacing.md))

            // Error banner
            uiState.errorMessage?.let { error ->
                PcCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.clearError() }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = PcAccentCopper,
                            modifier = Modifier.height(18.dp)
                        )
                        Spacer(modifier = Modifier.width(PcSpacing.sm))
                        Text(
                            error,
                            style = MaterialTheme.typography.bodySmall,
                            color = PcAccentCopper
                        )
                    }
                }
                Spacer(modifier = Modifier.height(PcSpacing.sm))
            }

            // Content
            when (uiState.selectedTab) {
                AnalysisTab.TREND -> TrendTabContent(uiState, viewModel)
                AnalysisTab.VOLUME -> VolumeTabContent(uiState, viewModel)
                AnalysisTab.REPORT -> ReportTabContent(uiState, viewModel)
            }
        }
    }
}

// ─── Trend Tab ────────────────────────────────────────────

@Composable
private fun TrendTabContent(
    uiState: AnalysisUiState,
    viewModel: AnalysisViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Time range selector
        TimeRangeSelector(
            selected = uiState.timeRange,
            onSelect = { viewModel.setTimeRange(it) }
        )
        Spacer(modifier = Modifier.height(PcSpacing.md))

        // Body part chip selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PcSpacing.xs)
        ) {
            com.privatecoach.app.core.model.BodyPart.selectableList.forEach { bp ->
                val selected = bp in uiState.selectedBodyParts
                Box(
                    modifier = Modifier
                        .border(1.dp, if (selected) PcAccentCopper else PcDivider, PcShapes.extraSmall)
                        .clickable { viewModel.toggleBodyPart(bp) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        bp.chineseName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) PcAccentCopper else PcTextSecondary
                    )
                }
            }
        }
        Text(
            "最多选择3个部位",
            style = MaterialTheme.typography.labelSmall,
            color = PcTextDisabled,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(PcSpacing.md))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                PcLoadingIndicator()
            }
        } else if (uiState.selectedBodyParts.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                PcEmptyState(message = "选择一个训练部位开始分析趋势")
            }
        } else {
            Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                // Aggregated body part trend line
                if (uiState.bodyPartTrendLines.isNotEmpty()) {
                    Text(
                        "部位容量趋势",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(PcSpacing.sm))
                    PcLineChart(
                        lines = uiState.bodyPartTrendLines,
                        xAxisLabels = uiState.trendXLabels,
                        showLegend = true,
                        formatValue = { "%.0f kg".format(it) }
                    )
                    Spacer(modifier = Modifier.height(PcSpacing.lg))
                }

                // Exercise breakdown (single body part only)
                if (uiState.exerciseBreakdown.isNotEmpty()) {
                    val bp = uiState.selectedBodyParts.first()
                    Text(
                        "${bp.chineseName}动作容量拆解",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(PcSpacing.sm))
                    PcBarChart(
                        bars = uiState.exerciseBreakdown,
                        showValues = true,
                        valueLabelTransform = { "%.0f".format(it) }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ─── Volume Tab ────────────────────────────────────────────

@Composable
private fun VolumeTabContent(
    uiState: AnalysisUiState,
    viewModel: AnalysisViewModel
) {
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PcLoadingIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Time range selector
        TimeRangeSelector(
            selected = uiState.timeRange,
            onSelect = { viewModel.setTimeRange(it) }
        )
        Spacer(modifier = Modifier.height(PcSpacing.md))

        // Summary stats row
        PcCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PcStatValue(
                    value = "${uiState.monthlyTrainingDays}",
                    label = "训练天数",
                    highlighted = true
                )
                PcStatValue(
                    value = "${uiState.monthlyTotalSets}",
                    label = "总组数"
                )
                PcStatValue(
                    value = "%.0f kg".format(uiState.monthlyTotalVolume),
                    label = "总容量"
                )
            }
        }
        Spacer(modifier = Modifier.height(PcSpacing.lg))

        // Weekly volume bar chart
        if (uiState.weeklyVolumes.isNotEmpty()) {
            Text(
                "每周训练组数",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(PcSpacing.sm))

            val barData = uiState.weeklyVolumes.map { vol ->
                ChartBar(
                    label = vol.weekStart.format(DateTimeFormatter.ofPattern("M/d")),
                    value = vol.totalSets.toFloat(),
                    color = PcAccentCopper
                )
            }
            PcBarChart(bars = barData)
            Spacer(modifier = Modifier.height(PcSpacing.lg))
        }

        // Body part pie chart
        if (uiState.bodyPartStats.isNotEmpty()) {
            Text(
                "训练部位分布",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(PcSpacing.sm))

            val total = uiState.bodyPartStats.sumOf { it.count }
            val pieSegments = uiState.bodyPartStats.map { stat ->
                ChartSegment(
                    label = stat.bodyPart,
                    value = stat.count.toFloat(),
                    color = getBodyPartColor(stat.bodyPart)
                )
            }
            PcPieChart(
                segments = pieSegments,
                centerText = "${total}次",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(PcSpacing.lg))
        }

        // Training frequency line chart
        if (uiState.trainingFrequency.isNotEmpty()) {
            Text(
                "训练频率",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(PcSpacing.sm))

            val dates = uiState.trainingFrequency.map { it.date }
            val freqLine = ChartLine(
                label = "训练天数",
                values = uiState.trainingFrequency.map { it.sessions.toDouble() },
                color = PcAccentCopper
            )
            val xLabels = dates.map { it.format(DateTimeFormatter.ofPattern("M/d")) }
            PcLineChart(
                lines = listOf(freqLine),
                xAxisLabels = xLabels,
                showLegend = false
            )
        }

        if (uiState.weeklyVolumes.isEmpty() && uiState.bodyPartStats.isEmpty()) {
            PcEmptyState(message = "暂无训练数据")
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ─── Report Tab ────────────────────────────────────────────

@Composable
private fun ReportTabContent(
    uiState: AnalysisUiState,
    viewModel: AnalysisViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(PcSpacing.md))

        // Generate button
        Button(
            onClick = { viewModel.generateReport() },
            enabled = !uiState.isGeneratingReport,
            colors = ButtonDefaults.buttonColors(
                containerColor = PcAccentCopper,
                contentColor = PcBackground
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (uiState.isGeneratingReport) "AI 正在生成报告..." else "生成本周报告",
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(PcSpacing.lg))

        if (uiState.isGeneratingReport) {
            PcLoadingIndicator()
            Spacer(modifier = Modifier.height(PcSpacing.sm))
            Text(
                "AI 正在分析您的训练数据...",
                style = MaterialTheme.typography.bodySmall,
                color = PcTextSecondary
            )
        }

        // Report error
        uiState.reportError?.let { error ->
            Spacer(modifier = Modifier.height(PcSpacing.sm))
            PcCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.clearError() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = PcAccentCopper,
                        modifier = Modifier.height(18.dp)
                    )
                    Spacer(modifier = Modifier.width(PcSpacing.sm))
                    Text(
                        error,
                        style = MaterialTheme.typography.bodySmall,
                        color = PcAccentCopper
                    )
                }
            }
        }

        // Report card
        uiState.currentReport?.let { report ->
            Spacer(modifier = Modifier.height(PcSpacing.md))
            ReportCard(report = report)
        }

        if (!uiState.isGeneratingReport && uiState.currentReport == null && uiState.reportError == null) {
            Spacer(modifier = Modifier.height(PcSpacing.xxl))
            PcEmptyState(message = "点击上方按钮生成训练报告")
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ─── Report Card ────────────────────────────────────────────

@Composable
private fun ReportCard(report: com.privatecoach.app.core.model.Report) {
    PcCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            report.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(PcSpacing.sm))
        PcLineDivider()
        Spacer(modifier = Modifier.height(PcSpacing.md))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PcStatValue(value = "${report.trainingDays}", label = "训练天数", highlighted = true)
            PcStatValue(value = "${report.totalExercises}", label = "总动作数")
            report.volumeChange?.let { change ->
                val pctStr = if (change >= 0) "+%.0f%%".format(change) else "%.0f%%".format(change)
                PcStatValue(value = pctStr, label = "容量变化")
            }
        }

        Spacer(modifier = Modifier.height(PcSpacing.md))

        // Top body parts
        if (report.topBodyParts.isNotEmpty()) {
            Text(
                "训练部位分布",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(PcSpacing.sm))
            report.topBodyParts.forEach { stat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stat.bodyPart,
                        style = MaterialTheme.typography.bodySmall,
                        color = PcTextSecondary,
                        modifier = Modifier.width(60.dp)
                    )
                    // Simple progress bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .border(1.dp, PcDivider, PcShapes.extraSmall),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(stat.percentage.coerceIn(0f, 1f))
                                .height(6.dp)
                                .background(getBodyPartColor(stat.bodyPart), PcShapes.extraSmall)
                        )
                    }
                    Spacer(modifier = Modifier.width(PcSpacing.sm))
                    Text(
                        "%.0f%%".format(stat.percentage * 100),
                        style = MaterialTheme.typography.labelSmall,
                        color = PcTextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(PcSpacing.md))
        }

        // Top progress exercises
        if (report.topProgressExercises.isNotEmpty()) {
            Text(
                "进步最大的动作",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(PcSpacing.sm))
            report.topProgressExercises.forEach { ex ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "▸",
                        color = PcAccentCopper,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(PcSpacing.xs))
                    Text(
                        ex,
                        style = MaterialTheme.typography.bodySmall,
                        color = PcTextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(PcSpacing.md))
        }

        // AI Summary
        report.aiSummary?.let { summary ->
            PcLineDivider()
            Spacer(modifier = Modifier.height(PcSpacing.md))
            Text(
                "AI 总结",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(PcSpacing.sm))
            Text(
                summary,
                style = MaterialTheme.typography.bodyMedium,
                color = PcTextSecondary
            )
        }
    }
}

// ─── Time Range Selector ────────────────────────────────────

@Composable
private fun TimeRangeSelector(
    selected: TimeRange,
    onSelect: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PcSpacing.sm)
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = range == selected
            Box(
                modifier = Modifier
                    .border(
                        1.dp,
                        if (isSelected) PcAccentCopper else PcDivider,
                        PcShapes.extraSmall
                    )
                    .clickable { onSelect(range) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    range.chineseName,
                    color = if (isSelected) PcAccentCopper else PcTextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}
