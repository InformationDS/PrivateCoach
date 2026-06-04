package com.privatecoach.app.ui.screen.record

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.privatecoach.app.core.audio.RecordingState
import com.privatecoach.app.ui.component.*
import com.privatecoach.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToManual: () -> Unit,
    onNavigateToConfirm: () -> Unit,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate to confirm when result is available
    LaunchedEffect(uiState.parsedResult) {
        if (uiState.parsedResult != null) onNavigateToConfirm()
    }

    Scaffold(
        containerColor = PcBackground,
        topBar = {
            TopAppBar(
                title = { Text("记录训练", fontWeight = FontWeight.Light, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回", tint = PcTextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PcBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = PcSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(PcSpacing.lg))

            // Input mode tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PcSpacing.sm)
            ) {
                listOf(InputTab.VOICE to "🎤 语音", InputTab.TEXT to "📝 文字", InputTab.MANUAL to "✍️ 手动").forEach { (mode, label) ->
                    val selected = uiState.inputMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, if (selected) PcAccentCopper else PcDivider, PcShapes.small)
                            .clickable {
                                if (mode == InputTab.MANUAL) onNavigateToManual()
                                else viewModel.setInputMode(mode)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (selected) PcAccentCopper else PcTextSecondary, fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(PcSpacing.lg))

            when (uiState.inputMode) {
                InputTab.VOICE -> VoiceRecordingPanel(
                    recordingState = uiState.recordingState,
                    amplitude = uiState.currentAmplitude,
                    elapsedSeconds = uiState.elapsedSeconds,
                    isProcessing = uiState.isProcessing,
                    errorMessage = uiState.errorMessage,
                    templates = uiState.templates,
                    selectedTemplateName = uiState.selectedTemplateName,
                    onStartRecording = { viewModel.startRecording() },
                    onPauseRecording = { viewModel.pauseRecording() },
                    onResumeRecording = { viewModel.resumeRecording() },
                    onStopRecording = { viewModel.stopRecording() },
                    onSelectTemplate = { viewModel.setTemplate(it) },
                    onDismissError = { viewModel.clearError() }
                )
                InputTab.TEXT -> TextInputPanel(
                    text = uiState.textInput,
                    isProcessing = uiState.isProcessing,
                    errorMessage = uiState.errorMessage,
                    onTextChange = { viewModel.setTextInput(it) },
                    onSendToAi = { viewModel.sendTextToAi() },
                    onDismissError = { viewModel.clearError() }
                )
                InputTab.MANUAL -> {} // Navigates away
            }
        }
    }
}

@Composable
private fun VoiceRecordingPanel(
    recordingState: RecordingState,
    amplitude: Int,
    elapsedSeconds: Int,
    isProcessing: Boolean,
    errorMessage: String?,
    templates: List<com.privatecoach.app.core.model.TrainingTemplate>,
    selectedTemplateName: String?,
    onStartRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onSelectTemplate: (String?) -> Unit,
    onDismissError: () -> Unit
) {
    val isRecording = recordingState == RecordingState.RECORDING
    val isPaused = recordingState == RecordingState.PAUSED

    // Pulsing animation when recording
    val pulseScale by animateFloatAsState(
        targetValue = if (isRecording) 1.08f else 1f,
        animationSpec = tween(600),
        label = "pulse"
    )

    // Template chips
    if (templates.isNotEmpty() && !isRecording && !isProcessing) {
        Text("选择模板（可选）", style = MaterialTheme.typography.labelMedium, color = PcTextSecondary)
        Spacer(modifier = Modifier.height(PcSpacing.sm))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(PcSpacing.sm)) {
            item {
                PcTag(
                    text = "不使用",
                    color = if (selectedTemplateName == null) PcAccentCopper else PcDivider,
                    modifier = Modifier.clickable { onSelectTemplate(null) }
                )
            }
            items(templates) { tmpl ->
                PcTag(
                    text = tmpl.name,
                    color = if (selectedTemplateName == tmpl.name) PcAccentCopper else PcDivider,
                    modifier = Modifier.clickable { onSelectTemplate(tmpl.name) }
                )
            }
        }
        Spacer(modifier = Modifier.height(PcSpacing.lg))
    }

    // Timer
    Text(
        text = formatSeconds(elapsedSeconds),
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Light,
        color = if (isRecording) PcAccentCopper else PcTextSecondary
    )

    Spacer(modifier = Modifier.height(PcSpacing.xl))

    // Waveform visualization
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = PcSpacing.lg)
    ) {
        // Simple bar visualization based on amplitude
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(20) { i ->
                val barHeight = if (isRecording || isPaused) {
                    // Create a dynamic waveform effect
                    val base = ((amplitude / 100f) * 72).dp
                    val variation = kotlin.math.sin((i * 0.5 + elapsedSeconds * 2.0).toFloat()).toDouble()
                    base * 0.3f + (base * 0.7f * kotlin.math.abs(variation).toFloat())
                } else 4.dp
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(barHeight.coerceIn(4.dp, 72.dp))
                        .clip(PcShapes.extraSmall)
                        .background(if (isRecording) PcAccentCopper else PcDivider)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(PcSpacing.xl))

    // Processing indicator
    if (isProcessing) {
        PcLoadingIndicator()
        Spacer(modifier = Modifier.height(PcSpacing.lg))
        Text("AI 正在解析训练内容...", style = MaterialTheme.typography.bodyMedium, color = PcTextSecondary)
        Spacer(modifier = Modifier.height(PcSpacing.lg))
    }

    // Error message
    errorMessage?.let { error ->
        PcCard(modifier = Modifier.fillMaxWidth().clickable { onDismissError() }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Warning, contentDescription = null, tint = PcFeelingTired, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(PcSpacing.sm))
                Text(error, style = MaterialTheme.typography.bodySmall, color = PcFeelingTired, modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(PcSpacing.md))
    }

    // Recording controls
    if (!isProcessing) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(PcSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start / Pause-Resume / Stop
            when {
                recordingState == RecordingState.IDLE || recordingState == RecordingState.STOPPED -> {
                    // Start button
                    FloatingActionButton(
                        onClick = onStartRecording,
                        containerColor = PcAccentCopper,
                        contentColor = PcBackground,
                        modifier = Modifier.size(72.dp).scale(pulseScale),
                        shape = PcShapes.medium
                    ) {
                        Icon(Icons.Outlined.Mic, contentDescription = "开始录音", modifier = Modifier.size(32.dp))
                    }
                }
                isRecording -> {
                    // Pause button
                    IconButton(onClick = onPauseRecording, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.PauseCircle, contentDescription = "暂停", tint = PcTextSecondary, modifier = Modifier.size(36.dp))
                    }
                    // Stop button
                    IconButton(onClick = onStopRecording, modifier = Modifier.size(72.dp)) {
                        Icon(Icons.Outlined.StopCircle, contentDescription = "停止并分析", tint = PcAccentCopper, modifier = Modifier.size(48.dp))
                    }
                }
                isPaused -> {
                    // Resume button
                    IconButton(onClick = onResumeRecording, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Outlined.PlayCircle, contentDescription = "继续", tint = PcTextSecondary, modifier = Modifier.size(36.dp))
                    }
                    // Stop button
                    IconButton(onClick = onStopRecording, modifier = Modifier.size(72.dp)) {
                        Icon(Icons.Outlined.StopCircle, contentDescription = "停止并分析", tint = PcAccentCopper, modifier = Modifier.size(48.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(PcSpacing.md))
        Text(
            text = when (recordingState) {
                RecordingState.IDLE -> "点击开始录音"
                RecordingState.RECORDING -> "暂停 / 完成"
                RecordingState.PAUSED -> "继续 / 完成"
                else -> ""
            },
            style = MaterialTheme.typography.bodySmall,
            color = PcTextDisabled
        )
    }

    Spacer(modifier = Modifier.height(PcSpacing.xxl))
}

@Composable
private fun TextInputPanel(
    text: String,
    isProcessing: Boolean,
    errorMessage: String?,
    onTextChange: (String) -> Unit,
    onSendToAi: () -> Unit,
    onDismissError: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).border(1.dp, PcDivider, PcShapes.small).padding(PcSpacing.md)) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxSize(),
                placeholder = { Text("描述你的训练...\n例如：今天练胸，卧推60公斤4组8次，飞鸟15公斤3组12次，感觉还不错", color = PcTextDisabled) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedContainerColor = PcBackground,
                    unfocusedContainerColor = PcBackground,
                    cursorColor = PcAccentCopper,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(lineHeight = MaterialTheme.typography.bodyMedium.lineHeight)
            )
        }

        Spacer(modifier = Modifier.height(PcSpacing.lg))

        if (isProcessing) {
            PcLoadingIndicator()
            Spacer(modifier = Modifier.height(PcSpacing.md))
            Text("AI 正在解析...", style = MaterialTheme.typography.bodyMedium, color = PcTextSecondary)
        }

        errorMessage?.let { error ->
            PcCard(modifier = Modifier.fillMaxWidth().clickable { onDismissError() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = PcFeelingTired, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(PcSpacing.sm))
                    Text(error, style = MaterialTheme.typography.bodySmall, color = PcFeelingTired, modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(PcSpacing.md))
        }

        Button(
            onClick = onSendToAi,
            enabled = text.isNotBlank() && !isProcessing,
            colors = ButtonDefaults.buttonColors(containerColor = PcAccentCopper, contentColor = PcBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("发送给 AI 解析")
        }
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
