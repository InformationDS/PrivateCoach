package com.privatecoach.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
) {
    DASHBOARD(Screen.Dashboard, "首页", Icons.Outlined.Home),
    CALENDAR(Screen.Calendar, "日历", Icons.Outlined.DateRange),
    ANALYSIS(Screen.Analysis, "分析", Icons.Outlined.CalendarMonth),
    HISTORY(Screen.History, "记录", Icons.Outlined.ListAlt),
    SETTINGS(Screen.Settings, "设置", Icons.Outlined.Settings)
}
