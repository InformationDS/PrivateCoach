package com.privatecoach.app.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcSpacing

@Composable
fun PcLoadingIndicator(modifier: Modifier = Modifier) {
    val alpha by rememberInfiniteTransition(label = "loading").animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loadingAlpha"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .padding(horizontal = PcSpacing.lg)
            .clip(PcShapes.extraSmall)
            .background(PcAccentCopper.copy(alpha = alpha))
    )
}
