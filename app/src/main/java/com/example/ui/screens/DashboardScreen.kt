package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.util.DateUtils
import com.example.data.model.Priority
import com.example.data.model.TaskItem
import com.example.data.model.TaskStatus
import com.example.service.AnalyticsService
import com.example.ui.AppDestination
import com.example.ui.MainTab
import com.example.ui.MomentumViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MomentumViewModel,
    onNavigateToTab: (MainTab) -> Unit,
    onNavigateToDestination: (AppDestination) -> Unit,
    onOpenAddTask: () -> Unit
) {
    val todayTasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val dueRevisions by viewModel.dueRevisions.collectAsStateWithLifecycle()
    val todayReview by viewModel.currentDayReview.collectAsStateWithLifecycle()
    val dailyFocusLast7Days by viewModel.dailyFocusLast7Days.collectAsStateWithLifecycle()

    val completedCount = todayTasks.count { it.status == TaskStatus.COMPLETED }
    val totalCount = todayTasks.size
    val highPriorities = todayTasks.filter { it.priority == Priority.HIGH }

    val todayFocusMinutes = allSessions
        .filter { DateUtils.dateFromMillis(it.startedAt) == DateUtils.todayString() && it.focusedSeconds > 0 }
        .sumOf { it.focusedSeconds } / 60

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // HEADER & MOTTO
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MOMENTUM",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = DateUtils.formatFullDate(DateUtils.todayString()),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row {
                            IconButton(
                                onClick = { onNavigateToDestination(AppDestination.Revision) },
                                modifier = Modifier.testTag("dashboard_revision_shortcut")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (dueRevisions.isNotEmpty()) {
                                            Badge { Text("${dueRevisions.size}") }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = "Spaced Revision",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onNavigateToDestination(AppDestination.Goals) },
                                modifier = Modifier.testTag("dashboard_goals_shortcut")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = "Goals",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Plan → Execute → Record → Analyze → Improve",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // STATS SUMMARY ROW
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Tasks Planned",
                        value = if (totalCount == 0) "0" else "$completedCount/$totalCount",
                        supportingText = if (totalCount == 0) "No tasks yet" else "${((completedCount.toFloat() / totalCount) * 100).toInt()}% completed",
                        icon = Icons.Default.CheckCircle,
                        valueColor = if (completedCount > 0) MomentumGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Focus Time",
                        value = "${todayFocusMinutes}m",
                        supportingText = if (todayFocusMinutes > 0) "Recorded today" else "Start a session",
                        icon = Icons.Default.Timer,
                        valueColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Due Revisions",
                        value = "${dueRevisions.size}",
                        supportingText = if (dueRevisions.isNotEmpty()) "Ready for review" else "All caught up",
                        icon = Icons.Default.School,
                        valueColor = if (dueRevisions.isNotEmpty()) MomentumAmber else MomentumGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Daily Energy",
                        value = if (todayReview != null) "${todayReview!!.energyRating}/5 ★" else "—",
                        supportingText = if (todayReview != null) "Reflection saved" else "Not recorded yet",
                        icon = Icons.Default.Bolt,
                        valueColor = MomentumAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ACTION SHORTCUT BUTTONS
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigateToTab(MainTab.FOCUS) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("dashboard_start_focus_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Focus", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenAddTask,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("dashboard_add_task_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Task", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // TODAY'S TOP PRIORITIES
        item {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Top Priorities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { onNavigateToTab(MainTab.PLANNER) }) {
                        Text("View Planner")
                    }
                }
            }
        }

        if (highPriorities.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    EmptyStateView(
                        icon = Icons.Default.StarOutline,
                        title = "No High Priorities for Today",
                        message = "Define 1-3 essential priorities to focus your attention on what matters most.",
                        actionLabel = "Add High Priority Task",
                        onAction = onOpenAddTask
                    )
                }
            }
        } else {
            items(highPriorities) { task ->
                PriorityTaskCard(
                    task = task,
                    onStatusChange = { newStatus ->
                        viewModel.updateTaskStatus(task, newStatus)
                    },
                    onStartFocus = {
                        viewModel.startFocusSession(
                            targetMinutes = task.estimatedMinutes.takeIf { it > 0 } ?: 25,
                            taskId = task.id,
                            taskTitle = task.title
                        )
                        onNavigateToTab(MainTab.FOCUS)
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // WEEKLY FOCUS PROGRESS
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weekly Focus Minutes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = { onNavigateToTab(MainTab.INSIGHTS) }) {
                            Text("Full Analytics")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    WeeklyFocusBarChart(days = dailyFocusLast7Days)
                }
            }
        }

        // DAILY REFLECTION SHORTCUT BANNER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToTab(MainTab.REVIEW) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (todayReview != null) "Daily Reflection Recorded" else "Record Today's Reflection",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (todayReview != null) "Tap to edit your insights" else "Energy, wins, difficulties, & tomorrow's priority",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityTaskCard(
    task: TaskItem,
    onStatusChange: (TaskStatus) -> Unit,
    onStartFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = task.status == TaskStatus.COMPLETED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("priority_task_card_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { checked ->
                    onStatusChange(if (checked) TaskStatus.COMPLETED else TaskStatus.PLANNED)
                },
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PriorityBadge(task.priority)
                    Text(
                        text = "${task.estimatedMinutes}m est" + if (task.actualMinutes > 0) " • ${task.actualMinutes}m actual" else "",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isCompleted) {
                IconButton(
                    onClick = onStartFocus,
                    modifier = Modifier.testTag("task_start_focus_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Focus on this task",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
