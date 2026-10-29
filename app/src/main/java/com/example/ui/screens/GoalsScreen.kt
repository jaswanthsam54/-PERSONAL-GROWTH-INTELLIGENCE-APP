package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.Goal
import com.example.data.model.TaskStatus
import com.example.ui.MomentumViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: MomentumViewModel,
    onBack: () -> Unit
) {
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()

    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<Goal?>(null) }

    val categories = listOf("All", "Coding", "College", "Projects", "Fitness", "Personal Development")
    val filteredGoals = allGoals.filter {
        selectedCategoryFilter == null || selectedCategoryFilter == "All" || it.category == selectedCategoryFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("goals_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddGoalDialog = true }, modifier = Modifier.testTag("goals_add_button")) {
                        Icon(Icons.Default.Add, contentDescription = "Add Goal")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("goals_fab_add")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("goals_screen")
        ) {
            // CATEGORY FILTER ROW
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = (selectedCategoryFilter == null && cat == "All") || selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = if (cat == "All") null else cat },
                        label = { Text(cat) }
                    )
                }
            }

            if (filteredGoals.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Flag,
                    title = "No Goals Found",
                    message = "Set meaningful targets across Coding, College, Projects, Fitness, and Personal Development.",
                    actionLabel = "Add Your First Goal",
                    onAction = { showAddGoalDialog = true }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredGoals, key = { it.id }) { goal ->
                        val linkedTasks = allTasks.filter { it.goalId == goal.id }
                        val completedLinked = linkedTasks.count { it.status == TaskStatus.COMPLETED }

                        GoalCard(
                            goal = goal,
                            totalTasks = linkedTasks.size,
                            completedTasks = completedLinked,
                            onEdit = { goalToEdit = goal },
                            onDelete = { viewModel.deleteGoal(goal) },
                            onToggleStatus = {
                                val nextStatus = if (goal.status == "ACTIVE") "COMPLETED" else "ACTIVE"
                                viewModel.updateGoal(goal.copy(status = nextStatus, updatedAt = System.currentTimeMillis()))
                            }
                        )
                    }
                }
            }
        }
    }

    // ADD GOAL DIALOG
    if (showAddGoalDialog) {
        GoalFormDialog(
            goal = null,
            onDismiss = { showAddGoalDialog = false },
            onSave = { title, desc, cat, targetDate, metric ->
                viewModel.addGoal(title, desc, cat, targetDate, metric)
                showAddGoalDialog = false
            }
        )
    }

    // EDIT GOAL DIALOG
    if (goalToEdit != null) {
        GoalFormDialog(
            goal = goalToEdit,
            onDismiss = { goalToEdit = null },
            onSave = { title, desc, cat, targetDate, metric ->
                viewModel.updateGoal(
                    goalToEdit!!.copy(
                        title = title,
                        description = desc,
                        category = cat,
                        targetDate = targetDate,
                        targetMetric = metric,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                goalToEdit = null
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    totalTasks: Int,
    completedTasks: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(goal.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleStatus) {
                        Icon(
                            imageVector = if (goal.status == "COMPLETED") Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Toggle status",
                            tint = if (goal.status == "COMPLETED") MomentumGreen else Slate400
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Goal")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (goal.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(goal.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(10.dp))
            if (goal.targetMetric.isNotEmpty()) {
                Text("Target: ${goal.targetMetric}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (totalTasks > 0) {
                val progress = completedTasks.toFloat() / totalTasks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Linked Tasks: $completedTasks/$totalTasks", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MomentumGreen,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if (goal.targetDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Target Date: ${DateUtils.formatDate(goal.targetDate)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun GoalFormDialog(
    goal: Goal?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(goal?.title ?: "") }
    var description by remember { mutableStateOf(goal?.description ?: "") }
    var category by remember { mutableStateOf(goal?.category ?: "Coding") }
    var targetDate by remember { mutableStateOf(goal?.targetDate ?: DateUtils.addDays(DateUtils.todayString(), 30)) }
    var targetMetric by remember { mutableStateOf(goal?.targetMetric ?: "") }

    val categories = listOf("Coding", "College", "Projects", "Fitness", "Personal Development")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal == null) "New Goal" else "Edit Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
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
                Text("Category:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetMetric,
                    onValueChange = { targetMetric = it },
                    label = { Text("Measurable Metric (e.g. 5 projects, 100km)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Target Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), description.trim(), category, targetDate.trim(), targetMetric.trim())
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
