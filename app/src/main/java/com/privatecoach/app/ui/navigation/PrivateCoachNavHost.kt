package com.privatecoach.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.privatecoach.app.ui.screen.calendar.CalendarScreen
import com.privatecoach.app.ui.screen.dashboard.DashboardScreen
import com.privatecoach.app.ui.screen.detail.WorkoutDetailScreen
import com.privatecoach.app.ui.screen.history.HistoryScreen
import com.privatecoach.app.ui.screen.record.ConfirmResultScreen
import com.privatecoach.app.ui.screen.record.ManualEntryScreen
import com.privatecoach.app.ui.screen.record.RecordScreen
import com.privatecoach.app.ui.screen.analysis.AnalysisScreen
import com.privatecoach.app.ui.screen.settings.SettingsScreen
import com.privatecoach.app.ui.screen.template.TemplateEditScreen
import com.privatecoach.app.ui.screen.template.TemplateListScreen
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcBackground
import com.privatecoach.app.ui.theme.PcTextSecondary

@Composable
fun PrivateCoachNavHost(
    navController: NavHostController = rememberNavController(),
    initialRoute: Screen = Screen.Dashboard
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if current route is a bottom-bar screen
    val bottomBarRoutes = setOf(
        Screen.Dashboard::class.qualifiedName,
        Screen.Calendar::class.qualifiedName,
        Screen.Analysis::class.qualifiedName,
        Screen.History::class.qualifiedName,
        Screen.Settings::class.qualifiedName
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        containerColor = PcBackground,
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = PcBackground,
                    tonalElevation = 0.dp
                ) {
                    BottomNavItem.entries.forEach { item ->
                        val selected = currentRoute == item.screen::class.qualifiedName
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) PcAccentCopper else PcTextSecondary
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    color = if (selected) PcAccentCopper else PcTextSecondary,
                                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = PcAccentCopper.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.Dashboard> {
                DashboardScreen(
                    onNavigateToRecord = { navController.navigate(Screen.Record) },
                    onNavigateToWorkoutDetail = { id -> navController.navigate(Screen.WorkoutDetail(id)) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings) }
                )
            }
            composable<Screen.Calendar> {
                CalendarScreen(
                    onNavigateToWorkoutDetail = { id -> navController.navigate(Screen.WorkoutDetail(id)) }
                )
            }
            composable<Screen.Analysis> {
                AnalysisScreen()
            }
            composable<Screen.History> {
                HistoryScreen(
                    onNavigateToWorkoutDetail = { id -> navController.navigate(Screen.WorkoutDetail(id)) }
                )
            }
            composable<Screen.Settings> {
                SettingsScreen(
                    onNavigateToTemplates = { navController.navigate(Screen.TemplateList) }
                )
            }
            composable<Screen.Record> {
                RecordScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToManual = { navController.navigate(Screen.ManualEntry()) },
                    onNavigateToConfirm = { navController.navigate(Screen.ConfirmResult) }
                )
            }
            composable<Screen.ConfirmResult> {
                ConfirmResultScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveComplete = { navController.popBackStack() }
                )
            }
            composable<Screen.ManualEntry> { backStackEntry ->
                ManualEntryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveComplete = { navController.popBackStack() }
                )
            }
            composable<Screen.WorkoutDetail> { backStackEntry ->
                WorkoutDetailScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.TemplateList> {
                TemplateListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate(Screen.TemplateEdit(id)) }
                )
            }
            composable<Screen.TemplateEdit> { backStackEntry ->
                TemplateEditScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
