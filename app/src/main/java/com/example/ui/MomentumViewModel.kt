package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.DateUtils
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.MomentumRepository
import com.example.service.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainTab(val label: String) {
    HOME("Home"),
    PLANNER("Planner"),
    FOCUS("Focus"),
    INSIGHTS("Insights"),
    REVIEW("Review")
}

sealed class AppDestination {
    data class Main(val tab: MainTab = MainTab.HOME) : AppDestination()
    object Goals : AppDestination()
    object Revision : AppDestination()
    object Distractions : AppDestination()
    object Settings : AppDestination()
}

class MomentumViewModel(application: Application) : AndroidViewModel(application) {
    val database = AppDatabase.getDatabase(application)
    val repository = MomentumRepository(database)
    val timerManager = FocusTimerManager(application, repository)

    // Navigation Stack
    private val _currentDestination = MutableStateFlow<AppDestination>(AppDestination.Main(MainTab.HOME))
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val navStack = mutableListOf<AppDestination>()

    fun navigateTo(dest: AppDestination) {
        navStack.add(_currentDestination.value)
        _currentDestination.value = dest
    }

    fun selectTab(tab: MainTab) {
        navStack.clear()
        _currentDestination.value = AppDestination.Main(tab)
    }

    fun navigateBack(): Boolean {
        return if (navStack.isNotEmpty()) {
            _currentDestination.value = navStack.removeAt(navStack.size - 1)
            true
        } else if (_currentDestination.value !is AppDestination.Main || (_currentDestination.value as AppDestination.Main).tab != MainTab.HOME) {
            _currentDestination.value = AppDestination.Main(MainTab.HOME)
            true
        } else {
            false
        }
    }

    // Selected Planner Date
    private val _selectedPlannerDate = MutableStateFlow(DateUtils.todayString())
    val selectedPlannerDate: StateFlow<String> = _selectedPlannerDate.asStateFlow()

    fun setPlannerDate(date: String) {
        _selectedPlannerDate.value = date
    }

    // Selected Review Date
    private val _selectedReviewDate = MutableStateFlow(DateUtils.todayString())
    val selectedReviewDate: StateFlow<String> = _selectedReviewDate.asStateFlow()

    fun setReviewDate(date: String) {
        _selectedReviewDate.value = date
    }

    // RAW FLOWS FROM REPOSITORY
    val allTasks = repository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allGoals = repository.allGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeGoals = repository.activeGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allSessions = repository.allSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDistractions = repository.allDistractions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTopics = repository.allTopics.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dueRevisions = repository.getDueRevisions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val upcomingRevisions = repository.getUpcomingRevisions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allRevisionLogs = repository.allRevisionLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDailyReviews = repository.allDailyReviews.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allExperiments = repository.allExperiments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeExperiments = repository.activeExperiments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Timer State
    val timerState = timerManager.timerState

    // Tasks for selected planner date
    val selectedDateTasks: StateFlow<List<TaskItem>> = combine(allTasks, _selectedPlannerDate) { tasks, date ->
        tasks.filter { it.plannedDate == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unfinished past tasks
    val unfinishedPastTasks: StateFlow<List<TaskItem>> = allTasks.map { tasks ->
        val today = DateUtils.todayString()
        tasks.filter { it.plannedDate < today && it.status != TaskStatus.COMPLETED && it.status != TaskStatus.SKIPPED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's tasks
    val todayTasks: StateFlow<List<TaskItem>> = allTasks.map { tasks ->
        val today = DateUtils.todayString()
        tasks.filter { it.plannedDate == today }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Review for selected review date
    val currentDayReview: StateFlow<DailyReview?> = combine(allDailyReviews, _selectedReviewDate) { reviews, date ->
        reviews.firstOrNull { it.reviewDate == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Derived Analytics & Insights
    val derivedInsights: StateFlow<List<PersonalInsight>> = combine(
        allTasks,
        allSessions,
        allDistractions,
        allRevisionLogs,
        allGoals
    ) { tasks, sessions, distractions, logs, goals ->
        InsightsEngine.generateInsights(tasks, sessions, distractions, logs, goals)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyComparison: StateFlow<WeeklyComparison> = combine(allTasks, allSessions) { tasks, sessions ->
        AnalyticsService.calculateWeeklyComparison(tasks, sessions)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WeeklyComparison(
            currentWeekStart = DateUtils.getStartOfWeek(),
            previousWeekStart = DateUtils.addDays(DateUtils.getStartOfWeek(), -7),
            currentFocusMinutes = 0,
            previousFocusMinutes = 0,
            focusDeltaMinutes = 0,
            currentCompletedPriorities = 0,
            currentTotalPriorities = 0,
            previousCompletedPriorities = 0,
            previousTotalPriorities = 0,
            currentPriorityRate = null,
            previousPriorityRate = null
        )
    )

    val dailyFocusLast7Days: StateFlow<List<DayFocusSummary>> = allSessions.map { sessions ->
        AnalyticsService.getDailyFocusLast7Days(sessions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val heatmapDays: StateFlow<List<HeatmapDay>> = combine(allTasks, allSessions) { tasks, sessions ->
        AnalyticsService.calculateActivityHeatmap(tasks, sessions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distractionSummaries: StateFlow<List<DistractionSummary>> = allDistractions.map {
        AnalyticsService.calculateDistractionSummaries(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ACTIONS
    fun addTask(
        title: String,
        description: String = "",
        plannedDate: String = DateUtils.todayString(),
        estimatedMinutes: Int = 30,
        priority: Priority = Priority.MEDIUM,
        goalId: Long? = null
    ) {
        viewModelScope.launch {
            repository.insertTask(
                TaskItem(
                    title = title,
                    description = description,
                    plannedDate = plannedDate,
                    estimatedMinutes = estimatedMinutes,
                    priority = priority,
                    goalId = goalId
                )
            )
        }
    }

    fun updateTask(task: TaskItem) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun updateTaskStatus(
        task: TaskItem,
        status: TaskStatus,
        actualMinutes: Int = task.actualMinutes,
        notes: String = task.completionNotes
    ) {
        viewModelScope.launch {
            repository.updateTaskStatus(task, status, actualMinutes, notes)
        }
    }

    fun rescheduleTask(task: TaskItem, newDate: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(plannedDate = newDate, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // GOALS
    fun addGoal(
        title: String,
        description: String,
        category: String,
        targetDate: String,
        targetMetric: String = ""
    ) {
        viewModelScope.launch {
            repository.insertGoal(
                Goal(
                    title = title,
                    description = description,
                    category = category,
                    targetDate = targetDate,
                    targetMetric = targetMetric
                )
            )
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // FOCUS TIMER
    fun startFocusSession(targetMinutes: Int, taskId: Long? = null, taskTitle: String? = null) {
        timerManager.startSession(targetMinutes, taskId, taskTitle)
    }

    fun pauseFocusSession() {
        timerManager.pauseSession()
    }

    fun resumeFocusSession() {
        timerManager.resumeSession()
    }

    fun finishFocusSession(interruptionNote: String? = null) {
        timerManager.finishSession(interruptionNote)
    }

    fun cancelFocusSession() {
        timerManager.cancelSession()
    }

    // DISTRACTIONS
    fun recordDistraction(
        category: String,
        durationMinutes: Int,
        note: String = "",
        taskId: Long? = null,
        focusSessionId: Long? = null
    ) {
        viewModelScope.launch {
            repository.insertDistraction(
                Distraction(
                    category = category,
                    durationMinutes = durationMinutes,
                    note = note,
                    taskId = taskId,
                    focusSessionId = focusSessionId
                )
            )
        }
    }

    fun deleteDistraction(distraction: Distraction) {
        viewModelScope.launch {
            repository.deleteDistraction(distraction)
        }
    }

    // LEARNING REVISION
    fun addLearningTopic(
        title: String,
        subject: String,
        goalId: Long? = null
    ) {
        viewModelScope.launch {
            val today = DateUtils.todayString()
            val nextDate = DateUtils.addDays(today, 1) // 1 day after initial study
            repository.insertTopic(
                LearningTopic(
                    title = title,
                    subject = subject,
                    goalId = goalId,
                    initialStudyDate = today,
                    nextRevisionDate = nextDate,
                    currentIntervalIndex = 0
                )
            )
        }
    }

    fun recordRevisionAttempt(topic: LearningTopic, rating: RecallRating, notes: String = "") {
        viewModelScope.launch {
            repository.recordRevisionAttempt(topic, rating, notes)
        }
    }

    fun deleteTopic(topic: LearningTopic) {
        viewModelScope.launch {
            repository.deleteTopic(topic)
        }
    }

    // DAILY REFLECTION
    fun saveDailyReview(
        energyRating: Int,
        focusRating: Int,
        whatWentWell: String,
        mainDifficulty: String,
        lessonLearned: String,
        tomorrowPriority: String,
        date: String = _selectedReviewDate.value
    ) {
        viewModelScope.launch {
            val existing = repository.getReviewForDate(date)
            val review = DailyReview(
                id = existing?.id ?: 0,
                reviewDate = date,
                energyRating = energyRating,
                focusRating = focusRating,
                whatWentWell = whatWentWell,
                mainDifficulty = mainDifficulty,
                lessonLearned = lessonLearned,
                tomorrowPriority = tomorrowPriority,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveDailyReview(review)
        }
    }

    // EXPERIMENTS
    fun addExperiment(title: String, description: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            repository.insertExperiment(
                ImprovementExperiment(
                    title = title,
                    description = description,
                    startDate = startDate,
                    endDate = endDate,
                    status = "ACTIVE"
                )
            )
        }
    }

    fun updateExperimentStatus(experiment: ImprovementExperiment, newStatus: String, outcomeNotes: String = "") {
        viewModelScope.launch {
            repository.updateExperiment(
                experiment.copy(
                    status = newStatus,
                    outcomeNotes = outcomeNotes,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // DEMO DATA & CLEAR
    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedDemoData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    suspend fun getExportCsv(): String {
        return repository.generateCsvExport()
    }
}
