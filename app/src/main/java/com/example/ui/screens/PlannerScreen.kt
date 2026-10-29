package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.util.DateUtils
import com.example.data.model.*
import com.example.ui.MainTab
import com.example.ui.MomentumViewModel
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: MomentumViewModel,
    onNavigateToTab: (MainTab) -> Unit,
    onOpenAddTask: () -> Unit
) {
    val selectedDate by viewModel.selectedPlannerDate.collectAsStateWithLifecycle()
    val tasksForDate by viewModel.selectedDateTasks.collectAsStateWithLifecycle()
    val unfinishedPastTasks by viewModel.unfinishedPastTasks.collectAsStateWithLifecycle()
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()

    var selectedStatusFilter by remember { mutableStateOf<TaskStatus?>(null) }
    var selectedPriorityFilter by remember { mutableStateOf<Priority?>(null) }
    var taskToEdit by remember { mutableStateOf<TaskItem?>(null) }
    var taskToCompleteWithNotes by remember { mutableStateOf<TaskItem?>(null) }

    val filteredTasks = tasksForDate.filter { task ->
        (selectedStatusFilter == null || task.status == selectedStatusFilter) &&
                (selectedPriorityFilter == null || task.priority == selectedPriorityFilter)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = 12.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Task Planner",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onOpenAddTask, modifier = Modifier.testTag("planner_add_task_header_btn")) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Task", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // DATE PICKER / NAVIGATOR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.setPlannerDate(DateUtils.addDays(selectedDate, -1)) },
                        modifier = Modifier.testTag("planner_prev_day")
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { viewModel.setPlannerDate(DateUtils.todayString()) }
                    ) {
                        Text(
                            text = if (DateUtils.isDateToday(selectedDate)) "Today" else DateUtils.getDayOfWeek(selectedDate),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (DateUtils.isDateToday(selectedDate)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = DateUtils.formatDate(selectedDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setPlannerDate(DateUtils.addDays(selectedDate, 1)) },
                        modifier = Modifier.testTag("planner_next_day")
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // FILTER CHIPS ROW
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedStatusFilter == null && selectedPriorityFilter == null,
                            onClick = {
                                selectedStatusFilter = null
                                selectedPriorityFilter = null
                            },
                            label = { Text("All (${tasksForDate.size})") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedPriorityFilter == Priority.HIGH,
                            onClick = {
                                selectedPriorityFilter = if (selectedPriorityFilter == Priority.HIGH) null else Priority.HIGH
                            },
                            label = { Text("High Priority") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedStatusFilter == TaskStatus.PLANNED,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == TaskStatus.PLANNED) null else TaskStatus.PLANNED
                            },
                            label = { Text("Planned") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedStatusFilter == TaskStatus.COMPLETED,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == TaskStatus.COMPLETED) null else TaskStatus.COMPLETED
                            },
                            label = { Text("Completed") }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 68.dp)
                    .testTag("planner_fab_add_task")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("planner_task_list"),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // UNFINISHED PAST TASKS CARD
            if (DateUtils.isDateToday(selectedDate) && unfinishedPastTasks.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("unfinished_past_tasks_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MomentumAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Unfinished Past Tasks (${unfinishedPastTasks.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tasks from earlier dates that were not marked finished or skipped.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            unfinishedPastTasks.take(3).forEach { task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Planned for ${DateUtils.formatDate(task.plannedDate)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.rescheduleTask(task, DateUtils.todayString()) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Move to Today", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // FILTERED TASKS FOR SELECTED DATE
            if (filteredTasks.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.EventNote,
                        title = "No tasks for this date",
                        message = "Plan your tasks with realistic duration estimates and designated priorities.",
                        actionLabel = "Plan New Task",
                        onAction = onOpenAddTask,
                        modifier = Modifier.padding(top = 40.dp)
                    )
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    val goal = allGoals.firstOrNull { it.id == task.goalId }
                    PlannerTaskItemCard(
                        task = task,
                        goalTitle = goal?.title,
                        onStatusToggle = {
                            if (task.status == TaskStatus.COMPLETED) {
                                viewModel.updateTaskStatus(task, TaskStatus.PLANNED)
                            } else {
                                taskToCompleteWithNotes = task
                            }
                        },
                        onStartFocus = {
                            viewModel.startFocusSession(
                                targetMinutes = task.estimatedMinutes.takeIf { it > 0 } ?: 25,
                                taskId = task.id,
                                taskTitle = task.title
                            )
                            onNavigateToTab(MainTab.FOCUS)
                        },
                        onEdit = { taskToEdit = task },
                        onDelete = { viewModel.deleteTask(task) },
                        onRescheduleTomorrow = {
                            viewModel.rescheduleTask(task, DateUtils.addDays(task.plannedDate, 1))
                        },
                        onMarkSkipped = {
                            viewModel.updateTaskStatus(task, TaskStatus.SKIPPED)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    // COMPLETE TASK DIALOG (with actual minutes & notes)
    if (taskToCompleteWithNotes != null) {
        val task = taskToCompleteWithNotes!!
        var actualMin by remember { mutableStateOf(if (task.actualMinutes > 0) task.actualMinutes.toString() else task.estimatedMinutes.toString()) }
        var notes by remember { mutableStateOf(task.completionNotes) }
        var isPartial by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { taskToCompleteWithNotes = null },
            title = { Text("Complete Task: ${task.title}") },
            text = {
                Column {
                    Text(
                        "Record your actual execution duration and notes to calibrate time estimation analytics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = actualMin,
                        onValueChange = { actualMin = it },
                        label = { Text("Actual Duration (minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Completion Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPartial, onCheckedChange = { isPartial = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark as Partially Completed", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedMin = actualMin.toIntOrNull() ?: task.estimatedMinutes
                        viewModel.updateTaskStatus(
                            task = task,
                            status = if (isPartial) TaskStatus.PARTIALLY_COMPLETED else TaskStatus.COMPLETED,
                            actualMinutes = parsedMin,
                            notes = notes
                        )
                        taskToCompleteWithNotes = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToCompleteWithNotes = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // EDIT TASK DIALOG
    if (taskToEdit != null) {
        EditTaskDialog(
            task = taskToEdit!!,
            allGoals = allGoals,
            onDismiss = { taskToEdit = null },
            onSave = { updatedTask ->
                viewModel.updateTask(updatedTask)
                taskToEdit = null
            }
        )
    }
}

@Composable
fun PlannerTaskItemCard(
    task: TaskItem,
    goalTitle: String?,
    onStatusToggle: () -> Unit,
    onStartFocus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRescheduleTomorrow: () -> Unit,
    onMarkSkipped: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val isCompleted = task.status == TaskStatus.COMPLETED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("planner_task_item_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { onStatusToggle() },
                    modifier = Modifier.testTag("planner_task_check_${task.id}")
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Task options")
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Focus on Task") },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                            onClick = {
                                expandedMenu = false
                                onStartFocus()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Task") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                expandedMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reschedule to Tomorrow") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            onClick = {
                                expandedMenu = false
                                onRescheduleTomorrow()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark as Skipped") },
                            leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) },
                            onClick = {
                                expandedMenu = false
                                onMarkSkipped()
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                expandedMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PriorityBadge(task.priority)
                    StatusBadge(task.status)
                    if (goalTitle != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = goalTitle,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                Text(
                    text = "${task.estimatedMinutes}m est" + if (task.actualMinutes > 0) " • ${task.actualMinutes}m actual" else "",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (task.completionNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${task.completionNotes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun EditTaskDialog(
    task: TaskItem,
    allGoals: List<Goal>,
    onDismiss: () -> Unit,
    onSave: (TaskItem) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var estimatedMin by remember { mutableStateOf(task.estimatedMinutes.toString()) }
    var actualMin by remember { mutableStateOf(task.actualMinutes.toString()) }
    var priority by remember { mutableStateOf(task.priority) }
    var selectedGoalId by remember { mutableStateOf(task.goalId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = estimatedMin,
                        onValueChange = { estimatedMin = it },
                        label = { Text("Est. Minutes") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = actualMin,
                        onValueChange = { actualMin = it },
                        label = { Text("Actual Min") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Priority:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            task.copy(
                                title = title.trim(),
                                description = description.trim(),
                                estimatedMinutes = estimatedMin.toIntOrNull() ?: 30,
                                actualMinutes = actualMin.toIntOrNull() ?: 0,
                                priority = priority,
                                goalId = selectedGoalId,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
