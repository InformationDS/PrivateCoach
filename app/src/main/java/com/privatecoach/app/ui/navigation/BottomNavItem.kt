package com.privatecoach.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
) {
    CONVERSATION(Screen.Conversation, "AI助手", Icons.Outlined.Chat),
    DASHBOARD(Screen.Dashboard, "仪表盘", Icons.Outlined.Dashboard),
    CALENDAR(Screen.Calendar, "日历", Icons.Outlined.CalendarMonth)
}
