package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.util.DateUtils
import com.example.data.model.Goal
import com.example.data.model.Priority

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    initialDate: String = DateUtils.todayString(),
    allGoals: List<Goal>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, date: String, minutes: Int, priority: Priority, goalId: Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var plannedDate by remember { mutableStateOf(initialDate) }
    var estimatedMinutesStr by remember { mutableStateOf("30") }
    var priority by remember { mutableStateOf(Priority.HIGH) }
    var selectedGoalId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plan New Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g. Implement Raft Consensus election") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_task_title_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = plannedDate,
                        onValueChange = { plannedDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = estimatedMinutesStr,
                        onValueChange = { estimatedMinutesStr = it },
                        label = { Text("Est. Minutes") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text("Priority Level:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.label) }
                        )
                    }
                }

                if (allGoals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Link to Goal (optional):", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = selectedGoalId == null,
                                onClick = { selectedGoalId = null },
                                label = { Text("None") }
                            )
                        }
                        items(allGoals) { goal ->
                            FilterChip(
                                selected = selectedGoalId == goal.id,
                                onClick = { selectedGoalId = goal.id },
                                label = { Text(goal.title.take(18)) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val minutes = estimatedMinutesStr.toIntOrNull() ?: 30
                        onConfirm(title.trim(), description.trim(), plannedDate.trim(), minutes, priority, selectedGoalId)
                    }
                },
                modifier = Modifier.testTag("add_task_dialog_confirm_btn")
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
