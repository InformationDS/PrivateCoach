package com.privatecoach.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcShapes

@Composable
fun PcLineDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .clip(PcShapes.extraSmall)
            .background(PcDivider)
    )
}
