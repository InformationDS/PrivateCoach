package com.privatecoach.app.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcShapes
import com.privatecoach.app.ui.theme.PcSurface
import com.privatecoach.app.ui.theme.PcSpacing

@Composable
fun PcCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .border(1.dp, PcDivider, PcShapes.small)
            .padding(PcSpacing.md)
    ) {
        content()
    }
}
