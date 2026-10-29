package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.util.DateUtils
import com.example.ui.components.ActiveTimerFloatingBanner
import com.example.ui.components.AddTaskDialog
import com.example.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomentumApp(
    viewModel: MomentumViewModel
) {
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()

    var showGlobalAddTaskDialog by remember { mutableStateOf(false) }

    // Intercept back navigation
    BackHandler(enabled = true) {
        viewModel.navigateBack()
    }

    val currentMainTab = (currentDestination as? AppDestination.Main)?.tab ?: MainTab.HOME

    Scaffold(
        topBar = {
            if (currentDestination is AppDestination.Main) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Momentum",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.navigateTo(AppDestination.Settings) },
                            modifier = Modifier.testTag("appbar_settings_btn")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Persistent Active Focus Session Banner
                ActiveTimerFloatingBanner(
                    timerState = timerState,
                    onExpand = { viewModel.selectTab(MainTab.FOCUS) },
                    onPause = { viewModel.pauseFocusSession() },
                    onResume = { viewModel.resumeFocusSession() }
                )

                if (currentDestination is AppDestination.Main) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        NavigationBarItem(
                            selected = currentMainTab == MainTab.HOME,
                            onClick = { viewModel.selectTab(MainTab.HOME) },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            modifier = Modifier.testTag("nav_item_home")
                        )
                        NavigationBarItem(
                            selected = currentMainTab == MainTab.PLANNER,
                            onClick = { viewModel.selectTab(MainTab.PLANNER) },
                            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Planner") },
                            label = { Text("Planner") },
                            modifier = Modifier.testTag("nav_item_planner")
                        )
                        NavigationBarItem(
                            selected = currentMainTab == MainTab.FOCUS,
                            onClick = { viewModel.selectTab(MainTab.FOCUS) },
                            icon = { Icon(Icons.Default.Timer, contentDescription = "Focus") },
                            label = { Text("Focus") },
                            modifier = Modifier.testTag("nav_item_focus")
                        )
                        NavigationBarItem(
                            selected = currentMainTab == MainTab.INSIGHTS,
                            onClick = { viewModel.selectTab(MainTab.INSIGHTS) },
                            icon = { Icon(Icons.Default.Insights, contentDescription = "Insights") },
                            label = { Text("Insights") },
                            modifier = Modifier.testTag("nav_item_insights")
                        )
                        NavigationBarItem(
                            selected = currentMainTab == MainTab.REVIEW,
                            onClick = { viewModel.selectTab(MainTab.REVIEW) },
                            icon = { Icon(Icons.Default.RateReview, contentDescription = "Review") },
                            label = { Text("Review") },
                            modifier = Modifier.testTag("nav_item_review")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentDestination, label = "screen_transition") { dest ->
                when (dest) {
                    is AppDestination.Main -> {
                        when (dest.tab) {
                            MainTab.HOME -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToTab = { viewModel.selectTab(it) },
                                onNavigateToDestination = { viewModel.navigateTo(it) },
                                onOpenAddTask = { showGlobalAddTaskDialog = true }
                            )
                            MainTab.PLANNER -> PlannerScreen(
                                viewModel = viewModel,
                                onNavigateToTab = { viewModel.selectTab(it) },
                                onOpenAddTask = { showGlobalAddTaskDialog = true }
                            )
                            MainTab.FOCUS -> FocusScreen(
                                viewModel = viewModel,
                                onOpenDistractions = { viewModel.navigateTo(AppDestination.Distractions) }
                            )
                            MainTab.INSIGHTS -> InsightsScreen(
                                viewModel = viewModel
                            )
                            MainTab.REVIEW -> ReviewScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                    is AppDestination.Goals -> GoalsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                    is AppDestination.Revision -> RevisionScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                    is AppDestination.Distractions -> DistractionsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                    is AppDestination.Settings -> SettingsScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
            }
        }
    }

    // GLOBAL ADD TASK DIALOG
    if (showGlobalAddTaskDialog) {
        AddTaskDialog(
            initialDate = DateUtils.todayString(),
            allGoals = allGoals,
            onDismiss = { showGlobalAddTaskDialog = false },
            onConfirm = { title, desc, date, minutes, priority, goalId ->
                viewModel.addTask(title, desc, date, minutes, priority, goalId)
                showGlobalAddTaskDialog = false
            }
        )
    }
}
