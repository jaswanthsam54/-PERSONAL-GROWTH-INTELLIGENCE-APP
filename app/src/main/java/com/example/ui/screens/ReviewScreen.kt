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
import com.example.data.model.ImprovementExperiment
import com.example.service.AnalyticsService
import com.example.ui.MomentumViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: MomentumViewModel
) {
    var selectedReviewTab by remember { mutableIntStateOf(0) } // 0: Daily Reflection, 1: Weekly Review
    val selectedDate by viewModel.selectedReviewDate.collectAsStateWithLifecycle()
    val currentReview by viewModel.currentDayReview.collectAsStateWithLifecycle()
    val allReviews by viewModel.allDailyReviews.collectAsStateWithLifecycle()

    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val allDistractions by viewModel.allDistractions.collectAsStateWithLifecycle()
    val allRevisionLogs by viewModel.allRevisionLogs.collectAsStateWithLifecycle()
    val activeExperiments by viewModel.activeExperiments.collectAsStateWithLifecycle()
    val allExperiments by viewModel.allExperiments.collectAsStateWithLifecycle()
    val weeklyComparison by viewModel.weeklyComparison.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("review_screen")
    ) {
        // TOP APP BAR WITH TABS
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Column {
                        Text(
                            text = "REFLECTION & GROWTH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (selectedReviewTab == 0) "Daily Reflection" else "Weekly Review & Experiments",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                TabRow(
                    selectedTabIndex = selectedReviewTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedReviewTab == 0,
                        onClick = { selectedReviewTab = 0 },
                        text = { Text("Daily Reflection", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedReviewTab == 1,
                        onClick = { selectedReviewTab = 1 },
                        text = { Text("Weekly Review", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }

        if (selectedReviewTab == 0) {
            DailyReflectionContent(
                selectedDate = selectedDate,
                currentReview = currentReview,
                allReviews = allReviews,
                onDateChange = { viewModel.setReviewDate(it) },
                onSaveReview = { energy, focus, well, diff, lesson, tomorrow ->
                    viewModel.saveDailyReview(energy, focus, well, diff, lesson, tomorrow, selectedDate)
                }
            )
        } else {
            WeeklyReviewContent(
                weeklyComparison = weeklyComparison,
                allTasks = allTasks,
                allSessions = allSessions,
                allDistractions = allDistractions,
                allRevisionLogs = allRevisionLogs,
                activeExperiments = activeExperiments,
                allExperiments = allExperiments,
                onAddExperiment = { title, desc ->
                    viewModel.addExperiment(
                        title = title,
                        description = desc,
                        startDate = DateUtils.todayString(),
                        endDate = DateUtils.addDays(DateUtils.todayString(), 7)
                    )
                },
                onUpdateExperiment = { exp, status, notes ->
                    viewModel.updateExperimentStatus(exp, status, notes)
                }
            )
        }
    }
}

@Composable
fun DailyReflectionContent(
    selectedDate: String,
    currentReview: com.example.data.model.DailyReview?,
    allReviews: List<com.example.data.model.DailyReview>,
    onDateChange: (String) -> Unit,
    onSaveReview: (Int, Int, String, String, String, String) -> Unit
) {
    var energyRating by remember(currentReview, selectedDate) { mutableIntStateOf(currentReview?.energyRating ?: 3) }
    var focusRating by remember(currentReview, selectedDate) { mutableIntStateOf(currentReview?.focusRating ?: 3) }
    var whatWentWell by remember(currentReview, selectedDate) { mutableStateOf(currentReview?.whatWentWell ?: "") }
    var mainDifficulty by remember(currentReview, selectedDate) { mutableStateOf(currentReview?.mainDifficulty ?: "") }
    var lessonLearned by remember(currentReview, selectedDate) { mutableStateOf(currentReview?.lessonLearned ?: "") }
    var tomorrowPriority by remember(currentReview, selectedDate) { mutableStateOf(currentReview?.tomorrowPriority ?: "") }
    var showSavedToast by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("daily_reflection_content"),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 100.dp)
    ) {
        // DATE SELECTOR
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onDateChange(DateUtils.addDays(selectedDate, -1)) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (DateUtils.isDateToday(selectedDate)) "Today's Reflection" else DateUtils.getDayOfWeek(selectedDate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = DateUtils.formatDate(selectedDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onDateChange(DateUtils.addDays(selectedDate, 1)) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                }
            }
        }

        // RATINGS CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Daily Energy & Focus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Energy Level (1-5):", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { energyRating = star },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("energy_star_$star")
                            ) {
                                Icon(
                                    imageVector = if (star <= energyRating) Icons.Default.Bolt else Icons.Default.FlashOff,
                                    contentDescription = "$star Stars",
                                    tint = if (star <= energyRating) MomentumAmber else Slate400
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Perceived Focus Clarity (1-5):", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { focusRating = star },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("focus_star_$star")
                            ) {
                                Icon(
                                    imageVector = if (star <= focusRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star Stars",
                                    tint = if (star <= focusRating) MomentumIndigoLight else Slate400
                                )
                            }
                        }
                    }
                }
            }
        }

        // REFLECTION QUESTIONS CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Qualitative Observations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = whatWentWell,
                        onValueChange = { whatWentWell = it },
                        label = { Text("What went well today?") },
                        placeholder = { Text("Wins, completed priorities, or moments of flow...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_what_went_well"),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = mainDifficulty,
                        onValueChange = { mainDifficulty = it },
                        label = { Text("Main difficulty or friction point?") },
                        placeholder = { Text("Fatigue, distractions, vague task scopes...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_main_difficulty"),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = lessonLearned,
                        onValueChange = { lessonLearned = it },
                        label = { Text("One lesson learned?") },
                        placeholder = { Text("What would you adjust next time?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_lesson_learned"),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tomorrowPriority,
                        onValueChange = { tomorrowPriority = it },
                        label = { Text("Tomorrow's #1 Must-Win Priority") },
                        placeholder = { Text("The single highest leverage item for tomorrow") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tomorrow_priority"),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            onSaveReview(energyRating, focusRating, whatWentWell, mainDifficulty, lessonLearned, tomorrowPriority)
                            showSavedToast = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_reflection_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Reflection", fontWeight = FontWeight.Bold)
                    }

                    if (showSavedToast) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Reflection saved securely in local storage.",
                            color = MomentumGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyReviewContent(
    weeklyComparison: com.example.service.WeeklyComparison,
    allTasks: List<com.example.data.model.TaskItem>,
    allSessions: List<com.example.data.model.FocusSession>,
    allDistractions: List<com.example.data.model.Distraction>,
    allRevisionLogs: List<com.example.data.model.RevisionLog>,
    activeExperiments: List<ImprovementExperiment>,
    allExperiments: List<ImprovementExperiment>,
    onAddExperiment: (String, String) -> Unit,
    onUpdateExperiment: (ImprovementExperiment, String, String) -> Unit
) {
    var showNewExperimentDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("weekly_review_content"),
        contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 100.dp)
    ) {
        // SUMMARY BANNER
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Weekly Retrospective Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Focus Time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${weeklyComparison.currentFocusMinutes}m", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text("Priorities Completed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${weeklyComparison.currentCompletedPriorities}/${weeklyComparison.currentTotalPriorities}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MomentumGreen)
                        }
                        Column {
                            Text("Distractions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${allDistractions.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MomentumAmber)
                        }
                    }
                }
            }
        }

        // SUGGESTED EXPERIMENT CANDIDATES
        item {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                Text(
                    text = "Suggested Improvement Experiments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hypotheses to test over the coming week to optimize your routines.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val suggestions = listOf(
            Pair("Phone in Drawer during Morning Focus", "Eliminate phone distractions by placing the device out of physical sight during morning deep work blocks."),
            Pair("25% Planning Duration Buffer", "Add 25% extra buffer to estimated task durations to protect against unforeseen complexity."),
            Pair("Pre-Review Active Recall Quiz", "Write topic summaries from memory before checking notes to boost Leitner recall rates.")
        )

        items(suggestions) { (title, desc) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        FilledTonalButton(
                            onClick = { onAddExperiment(title, desc) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Adopt Experiment")
                        }
                    }
                }
            }
        }

        // ACTIVE EXPERIMENTS TRACKER
        item {
            Column(modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Experiments (${activeExperiments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showNewExperimentDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New Experiment")
                    }
                }
            }
        }

        if (activeExperiments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    EmptyStateView(
                        icon = Icons.Default.Science,
                        title = "No Running Experiments",
                        message = "Adopt a suggested experiment above or create a custom habit change to track this week.",
                        actionLabel = "Create Custom Experiment",
                        onAction = { showNewExperimentDialog = true }
                    )
                }
            }
        } else {
            items(activeExperiments, key = { it.id }) { exp ->
                var outcomeNotes by remember { mutableStateOf(exp.outcomeNotes) }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
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
                            Text(exp.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFD1FAE5))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(exp.status, color = MomentumGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(exp.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${DateUtils.formatDate(exp.startDate)} → ${DateUtils.formatDate(exp.endDate)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = outcomeNotes,
                            onValueChange = { outcomeNotes = it },
                            label = { Text("Outcome Observations / Notes") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { onUpdateExperiment(exp, "DISMISSED", outcomeNotes) }) {
                                Text("Dismiss")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { onUpdateExperiment(exp, "COMPLETED", outcomeNotes) }) {
                                Text("Complete & Record")
                            }
                        }
                    }
                }
            }
        }
    }

    // CUSTOM EXPERIMENT DIALOG
    if (showNewExperimentDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewExperimentDialog = false },
            title = { Text("New Improvement Experiment") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Experiment Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Hypothesis & Rules") },
                        placeholder = { Text("What change will you test for 7 days?") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onAddExperiment(title.trim(), desc.trim())
                            showNewExperimentDialog = false
                        }
                    }
                ) {
                    Text("Start Experiment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewExperimentDialog = false }) { Text("Cancel") }
            }
        )
    }
}
