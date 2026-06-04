package com.privatecoach.app.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcAccentTeal
import com.privatecoach.app.ui.theme.PcShapes

@Composable
fun PcTag(
    text: String,
    color: Color = PcAccentCopper,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = color,
        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        modifier = modifier
            .border(1.dp, color, PcShapes.extraSmall)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun StrengthTag(modifier: Modifier = Modifier) {
    PcTag(text = "力量", color = PcAccentCopper, modifier = modifier)
}

@Composable
fun CardioTag(modifier: Modifier = Modifier) {
    PcTag(text = "有氧", color = PcAccentTeal, modifier = modifier)
}
