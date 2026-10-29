package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.util.DateUtils
import com.example.data.model.FocusSession
import com.example.data.model.TaskItem
import com.example.data.model.TaskStatus
import com.example.ui.MomentumViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    viewModel: MomentumViewModel,
    onOpenDistractions: () -> Unit
) {
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val todayTasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()

    var selectedMinutes by remember { mutableIntStateOf(25) }
    var selectedTask by remember { mutableStateOf<TaskItem?>(null) }
    var showInterruptionDialog by remember { mutableStateOf(false) }
    var showTaskPicker by remember { mutableStateOf(false) }

    val todayCompletedSessions = allSessions.filter {
        DateUtils.dateFromMillis(it.startedAt) == DateUtils.todayString() && it.focusedSeconds > 0
    }
    val todayTotalFocusedMinutes = todayCompletedSessions.sumOf { it.focusedSeconds } / 60

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("focus_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // HEADER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FOCUS ENGINE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Deep Work Session",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenDistractions,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Distraction Log", fontSize = 11.sp)
                    }
                }
            }
        }

        // TIMER HERO CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // LINKED TASK SELECTION CHIP
                    Surface(
                        modifier = Modifier
                            .clickable { if (!timerState.isRunning && !timerState.isPaused) showTaskPicker = true }
                            .testTag("focus_select_task_chip"),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val displayTaskTitle = timerState.activeTaskTitle ?: selectedTask?.title ?: "Select Task to Focus (Optional)"
                            Text(
                                text = displayTaskTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!timerState.isRunning && !timerState.isPaused) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // CIRCULAR TIMER VISUALIZATION
                    val targetSeconds = (if (timerState.isRunning || timerState.isPaused) timerState.targetMinutes else selectedMinutes) * 60
                    val elapsed = timerState.elapsedSeconds
                    val progress = if (targetSeconds > 0) (elapsed.toFloat() / targetSeconds).coerceIn(0f, 1f) else 0f

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(220.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 10.dp,
                            trackColor = Color.Transparent
                        )

                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = if (timerState.isPaused) MomentumAmber else MaterialTheme.colorScheme.primary,
                            strokeWidth = 10.dp,
                            trackColor = Color.Transparent
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = DateUtils.formatSeconds(elapsed),
                                fontSize = 44.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("focus_timer_text")
                            )
                            Text(
                                text = when {
                                    timerState.isRunning -> "Target: ${timerState.targetMinutes}m"
                                    timerState.isPaused -> "Paused"
                                    else -> "Ready • ${selectedMinutes}m"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // PRESET DURATION SELECTOR (When timer is inactive)
                    if (!timerState.isRunning && !timerState.isPaused) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            items(listOf(15, 25, 45, 60, 90)) { mins ->
                                FilterChip(
                                    selected = selectedMinutes == mins,
                                    onClick = { selectedMinutes = mins },
                                    label = { Text("${mins}m") }
                                )
                            }
                        }
                    }

                    // TIMER CONTROLS
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!timerState.isRunning && !timerState.isPaused) {
                            Button(
                                onClick = {
                                    viewModel.startFocusSession(
                                        targetMinutes = selectedMinutes,
                                        taskId = selectedTask?.id,
                                        taskTitle = selectedTask?.title
                                    )
                                },
                                modifier = Modifier
                                    .height(52.dp)
                                    .padding(horizontal = 16.dp)
                                    .testTag("focus_start_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Focus", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            if (timerState.isRunning) {
                                FilledTonalButton(
                                    onClick = { viewModel.pauseFocusSession() },
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("focus_pause_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pause")
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.resumeFocusSession() },
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("focus_resume_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Resume")
                                }
                            }

                            Button(
                                onClick = { viewModel.finishFocusSession() },
                                colors = ButtonDefaults.buttonColors(containerColor = MomentumGreen),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("focus_finish_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Complete")
                            }

                            IconButton(
                                onClick = { viewModel.cancelFocusSession() },
                                modifier = Modifier.testTag("focus_cancel_button")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel Session", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    // INTERRUPT / LOG DISTRACTION QUICK BUTTON
                    if (timerState.isRunning || timerState.isPaused) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = { showInterruptionDialog = true },
                            modifier = Modifier.testTag("focus_log_interruption_btn")
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp), tint = MomentumAmber)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Record Interruption / Distraction", color = MomentumAmber, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // DAILY FOCUS SUMMARY STATS
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Today's Focused Time",
                    value = "${todayTotalFocusedMinutes}m",
                    supportingText = "${todayCompletedSessions.size} completed sessions",
                    icon = Icons.Default.TrendingUp,
                    valueColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Avg. Session Length",
                    value = if (todayCompletedSessions.isEmpty()) "0m" else "${todayTotalFocusedMinutes / todayCompletedSessions.size}m",
                    supportingText = "Across today's blocks",
                    icon = Icons.Default.Timelapse,
                    valueColor = MomentumGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // RECENT FOCUS SESSIONS HISTORY
        item {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)) {
                Text(
                    text = "Recent Focus Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        val completedHistory = allSessions.filter { it.focusedSeconds > 0 }.take(8)
        if (completedHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    EmptyStateView(
                        icon = Icons.Default.TimerOff,
                        title = "No Focus Sessions Yet",
                        message = "Start your first focus block to track focused minutes and build execution momentum."
                    )
                }
            }
        } else {
            items(completedHistory, key = { it.id }) { session ->
                val linkedTask = allTasks.firstOrNull { it.id == session.taskId }
                FocusSessionItemCard(
                    session = session,
                    taskTitle = linkedTask?.title,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }

    // TASK PICKER SHEET / DIALOG
    if (showTaskPicker) {
        val pendingTasks = allTasks.filter { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.SKIPPED }
        AlertDialog(
            onDismissRequest = { showTaskPicker = false },
            title = { Text("Select Task for Focus") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        ListItem(
                            headlineContent = { Text("None (Standalone Focus Block)") },
                            modifier = Modifier.clickable {
                                selectedTask = null
                                showTaskPicker = false
                            }
                        )
                        Divider()
                    }
                    items(pendingTasks) { task ->
                        ListItem(
                            headlineContent = { Text(task.title) },
                            supportingContent = { Text("${task.estimatedMinutes}m est • ${task.priority.label} Priority") },
                            modifier = Modifier.clickable {
                                selectedTask = task
                                selectedMinutes = task.estimatedMinutes.takeIf { it > 0 } ?: 25
                                showTaskPicker = false
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTaskPicker = false }) { Text("Close") }
            }
        )
    }

    // INTERRUPTION RECORDING DIALOG
    if (showInterruptionDialog) {
        var category by remember { mutableStateOf("phone_use") }
        var durationMin by remember { mutableStateOf("5") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showInterruptionDialog = false },
            title = { Text("Record Interruption") },
            text = {
                Column {
                    Text(
                        "Logging interruptions helps identify external triggers and improve focus durability.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Category:", style = MaterialTheme.typography.labelMedium)
                    val categories = listOf("phone_use", "notifications", "unclear_task", "fatigue", "environment", "other")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.replace('_', ' ').replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = durationMin,
                        onValueChange = { durationMin = it },
                        label = { Text("Interruption Duration (minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("What happened? (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = durationMin.toIntOrNull() ?: 5
                        viewModel.recordDistraction(
                            category = category,
                            durationMinutes = mins,
                            note = note,
                            taskId = timerState.activeTaskId,
                            focusSessionId = timerState.activeSessionId
                        )
                        showInterruptionDialog = false
                    }
                ) {
                    Text("Log Distraction")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInterruptionDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun FocusSessionItemCard(
    session: FocusSession,
    taskTitle: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = taskTitle ?: "Deep Work Block",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = DateUtils.formatDate(DateUtils.dateFromMillis(session.startedAt)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!session.interruptionNote.isNullOrEmpty()) {
                        Text(
                            text = "Interruption: ${session.interruptionNote}",
                            fontSize = 11.sp,
                            color = MomentumAmber
                        )
                    }
                }
            }

            Text(
                text = "${session.focusedSeconds / 60}m",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
