package com.privatecoach.app.ui.screen.conversation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.privatecoach.app.core.model.QuickActionChip
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcTextPrimary

@Composable
fun QuickActionChips(
    chips: List<QuickActionChip>,
    onChipClick: (QuickActionChip) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            androidx.compose.material3.Surface(
                shape = PcShapes.small,
                color = com.privatecoach.app.ui.theme.PcSurface,
                modifier = Modifier
                    .border(1.dp, PcDivider, PcShapes.small)
                    .clickable { onChipClick(chip) }
            ) {
                Text(
                    text = "${chip.emoji} ${chip.label}",
                    color = PcTextPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
