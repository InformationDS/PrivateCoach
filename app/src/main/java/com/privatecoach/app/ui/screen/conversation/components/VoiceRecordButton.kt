package com.privatecoach.app.ui.screen.conversation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcBackground
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcTextPrimary
import com.privatecoach.app.ui.theme.PcTextSecondary

@Composable
fun VoiceRecordButton(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isRecording) {
            Text(
                text = "松开发送，上滑取消",
                color = PcTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Recording indicator — tap to stop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .height(48.dp)
                    .border(1.dp, PcAccentCopper, PcShapes.medium)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onStopRecording() }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔴 录音中... 点击停止",
                    color = PcAccentCopper,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // Idle record button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp)
                    .height(48.dp)
                    .clip(PcShapes.medium)
                    .background(if (enabled) PcAccentCopper else PcDivider)
                    .pointerInput(enabled) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                onStartRecording()
                                tryAwaitRelease()
                                if (isPressed) {
                                    onStopRecording()
                                }
                                isPressed = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (enabled) "🎤 按住录音" else "AI 暂不可用",
                    color = PcBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "─────── 或者 ───────",
            color = PcTextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
