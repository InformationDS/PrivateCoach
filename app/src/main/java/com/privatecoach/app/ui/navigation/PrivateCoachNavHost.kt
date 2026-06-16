package com.privatecoach.app.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.privatecoach.app.ui.screen.analysis.AnalysisScreen
import com.privatecoach.app.ui.screen.calendar.CalendarScreen
import com.privatecoach.app.ui.screen.conversation.ConversationScreen
import com.privatecoach.app.ui.screen.dashboard.DashboardScreen
import com.privatecoach.app.ui.screen.detail.WorkoutDetailScreen

import com.privatecoach.app.ui.screen.record.ManualEntryScreen
import com.privatecoach.app.ui.screen.settings.SettingsScreen
import com.privatecoach.app.ui.screen.template.TemplateEditScreen
import com.privatecoach.app.ui.screen.template.TemplateListScreen
import com.privatecoach.app.ui.theme.PcAccentCopper
import com.privatecoach.app.ui.theme.PcBackground
import com.privatecoach.app.ui.theme.PcDivider
import com.privatecoach.app.ui.theme.PcTextPrimary
import com.privatecoach.app.ui.theme.PcTextSecondary
import kotlinx.coroutines.launch

@Composable
fun PrivateCoachNavHost(
    navController: NavHostController = rememberNavController(),
    initialRoute: Screen = Screen.Conversation
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val bottomBarRoutes = setOf(
        Screen.Conversation::class.qualifiedName,
        Screen.Dashboard::class.qualifiedName,
        Screen.Calendar::class.qualifiedName
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = PcBackground
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Drawer header
                Text(
                    text = "PrivateCoach",
                    color = PcAccentCopper,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                HorizontalDivider(color = PcDivider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Drawer items
                DrawerItem("📈", "分析报告") {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.Analysis) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                DrawerItem("📝", "模板管理") {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.TemplateList) {
                        launchSingleTop = true
                    }
                }
                DrawerItem("⚙️", "设置") {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.Settings) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    ) {
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
                // Bottom tab screens
                composable<Screen.Conversation> {
                    ConversationScreen(
                        onNavigateToDashboard = { navController.navigate(Screen.Dashboard) },
                        onNavigateToCalendar = { navController.navigate(Screen.Calendar) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings) },
                        onNavigateToManualEntry = { navController.navigate(Screen.ManualEntry()) },
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }
                composable<Screen.Calendar> {
                    CalendarScreen(
                        onNavigateToWorkoutDetail = { id -> navController.navigate(Screen.WorkoutDetail(id)) }
                    )
                }

                // Drawer/menu screens
                composable<Screen.Dashboard> {
                    DashboardScreen(
                        onNavigateToRecord = { navController.navigate(Screen.ManualEntry()) },
                        onNavigateToWorkoutDetail = { id -> navController.navigate(Screen.WorkoutDetail(id)) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings) }
                    )
                }
                composable<Screen.Analysis> {
                    AnalysisScreen()
                }
                composable<Screen.TemplateList> {
                    TemplateListScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEdit = { id -> navController.navigate(Screen.TemplateEdit(id)) }
                    )
                }
                composable<Screen.Settings> {
                    SettingsScreen(
                        onNavigateToTemplates = { navController.navigate(Screen.TemplateList) }
                    )
                }

                // Full-screen destinations
                composable<Screen.WorkoutDetail> { backStackEntry ->
                    WorkoutDetailScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable<Screen.ManualEntry> { backStackEntry ->
                    ManualEntryScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onSaveComplete = { navController.popBackStack() }
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
}

@Composable
private fun DrawerItem(emoji: String, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp)
    ) {
        Text(
            text = emoji,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = PcTextPrimary,
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
        )
    }
}
