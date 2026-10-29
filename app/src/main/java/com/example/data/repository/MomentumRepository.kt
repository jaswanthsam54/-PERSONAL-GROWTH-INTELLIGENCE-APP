package com.example.data.repository

import com.example.core.util.DateUtils
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MomentumRepository(private val database: AppDatabase) {
    private val goalDao = database.goalDao()
    private val taskDao = database.taskDao()
    private val focusSessionDao = database.focusSessionDao()
    private val distractionDao = database.distractionDao()
    private val learningTopicDao = database.learningTopicDao()
    private val revisionLogDao = database.revisionLogDao()
    private val dailyReviewDao = database.dailyReviewDao()
    private val experimentDao = database.experimentDao()

    // GOALS
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()
    val activeGoals: Flow<List<Goal>> = goalDao.getGoalsByStatus("ACTIVE")
    suspend fun insertGoal(goal: Goal) = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: Goal) = goalDao.deleteGoal(goal)

    // TASKS
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()
    fun getTasksForDate(date: String): Flow<List<TaskItem>> = taskDao.getTasksForDate(date)
    fun getUnfinishedPastTasks(date: String = DateUtils.todayString()): Flow<List<TaskItem>> =
        taskDao.getUnfinishedPastTasks(date)
    suspend fun insertTask(task: TaskItem): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskItem) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskItem) = taskDao.deleteTask(task)
    suspend fun getTaskById(id: Long) = taskDao.getTaskById(id)

    suspend fun updateTaskStatus(task: TaskItem, newStatus: TaskStatus, actualMinutes: Int = task.actualMinutes, notes: String = task.completionNotes) {
        val updated = task.copy(
            status = newStatus,
            actualMinutes = actualMinutes,
            completionNotes = notes,
            completedAt = if (newStatus == TaskStatus.COMPLETED) System.currentTimeMillis() else task.completedAt,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)
    }

    // FOCUS SESSIONS
    val allSessions: Flow<List<FocusSession>> = focusSessionDao.getAllSessions()
    val activeSessionFlow: Flow<FocusSession?> = focusSessionDao.getActiveSessionFlow()
    suspend fun getActiveSession(): FocusSession? = focusSessionDao.getActiveSession()
    suspend fun insertSession(session: FocusSession): Long = focusSessionDao.insertSession(session)
    suspend fun updateSession(session: FocusSession) = focusSessionDao.updateSession(session)
    suspend fun deleteSession(session: FocusSession) = focusSessionDao.deleteSession(session)

    // DISTRACTIONS
    val allDistractions: Flow<List<Distraction>> = distractionDao.getAllDistractions()
    suspend fun insertDistraction(distraction: Distraction): Long = distractionDao.insertDistraction(distraction)
    suspend fun deleteDistraction(distraction: Distraction) = distractionDao.deleteDistraction(distraction)

    // LEARNING REVISION
    val allTopics: Flow<List<LearningTopic>> = learningTopicDao.getAllTopics()
    fun getDueRevisions(date: String = DateUtils.todayString()): Flow<List<LearningTopic>> =
        learningTopicDao.getDueRevisions(date)
    fun getUpcomingRevisions(date: String = DateUtils.todayString()): Flow<List<LearningTopic>> =
        learningTopicDao.getUpcomingRevisions(date)
    val allRevisionLogs: Flow<List<RevisionLog>> = revisionLogDao.getAllLogs()

    suspend fun insertTopic(topic: LearningTopic): Long = learningTopicDao.insertTopic(topic)
    suspend fun updateTopic(topic: LearningTopic) = learningTopicDao.updateTopic(topic)
    suspend fun deleteTopic(topic: LearningTopic) = learningTopicDao.deleteTopic(topic)

    /**
     * Submit revision review attempt and schedule next revision based on spaced repetition
     */
    suspend fun recordRevisionAttempt(
        topic: LearningTopic,
        rating: RecallRating,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val today = DateUtils.todayString()
        val log = RevisionLog(
            learningTopicId = topic.id,
            dueDate = topic.nextRevisionDate,
            completedAt = System.currentTimeMillis(),
            recallRating = rating.name,
            notes = notes
        )
        revisionLogDao.insertLog(log)

        // Spaced repetition progression
        val nextDate: String
        val nextIndex: Int

        when (rating) {
            RecallRating.RECALLED_SUCCESSFULLY -> {
                when (topic.currentIntervalIndex) {
                    0 -> { // Day 1 completed -> Next is Day 4 (+3 days)
                        nextDate = DateUtils.addDays(today, 3)
                        nextIndex = 1
                    }
                    1 -> { // Day 4 completed -> Next is Day 7 (+3 days)
                        nextDate = DateUtils.addDays(today, 3)
                        nextIndex = 2
                    }
                    2 -> { // Day 7 completed -> Next is Day 14 (+7 days)
                        nextDate = DateUtils.addDays(today, 7)
                        nextIndex = 3
                    }
                    else -> { // Mastered -> Next in 21 days
                        nextDate = DateUtils.addDays(today, 21)
                        nextIndex = topic.currentIntervalIndex + 1
                    }
                }
            }
            RecallRating.RECALLED_WITH_HINTS -> {
                // Repeat in 2 days to reinforce
                nextDate = DateUtils.addDays(today, 2)
                nextIndex = topic.currentIntervalIndex
            }
            RecallRating.NEEDED_RELEARNING -> {
                // Reset to 1 day later
                nextDate = DateUtils.addDays(today, 1)
                nextIndex = 0
            }
        }

        learningTopicDao.updateTopic(
            topic.copy(
                nextRevisionDate = nextDate,
                currentIntervalIndex = nextIndex,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // DAILY REVIEWS
    val allDailyReviews: Flow<List<DailyReview>> = dailyReviewDao.getAllReviews()
    fun getReviewForDateFlow(date: String): Flow<DailyReview?> = dailyReviewDao.getReviewForDateFlow(date)
    suspend fun getReviewForDate(date: String): DailyReview? = dailyReviewDao.getReviewForDate(date)
    suspend fun saveDailyReview(review: DailyReview): Long = dailyReviewDao.insertOrUpdateReview(review)

    // IMPROVEMENT EXPERIMENTS
    val allExperiments: Flow<List<ImprovementExperiment>> = experimentDao.getAllExperiments()
    val activeExperiments: Flow<List<ImprovementExperiment>> = experimentDao.getExperimentsByStatus("ACTIVE")
    suspend fun insertExperiment(experiment: ImprovementExperiment): Long = experimentDao.insertExperiment(experiment)
    suspend fun updateExperiment(experiment: ImprovementExperiment) = experimentDao.updateExperiment(experiment)
    suspend fun deleteExperiment(experiment: ImprovementExperiment) = experimentDao.deleteExperiment(experiment)

    // CLEAR ALL DATA
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        database.clearAllTables()
    }

    // CSV EXPORT GENERATOR
    suspend fun generateCsvExport(): String = withContext(Dispatchers.IO) {
        val tasks = taskDao.getAllTasksDirect()
        val sessions = focusSessionDao.getAllSessionsDirect()
        val distractions = distractionDao.getAllDistractionsDirect()
        val reviews = dailyReviewDao.getAllReviewsDirect()

        val sb = StringBuilder()
        sb.append("=== MOMENTUM PERSONAL DATA EXPORT ===\n")
        sb.append("Generated on: ").append(DateUtils.todayString()).append("\n\n")

        sb.append("--- TASKS ---\n")
        sb.append("ID,Title,PlannedDate,Priority,Status,EstimatedMin,ActualMin,Notes\n")
        for (t in tasks) {
            sb.append("${t.id},\"${t.title.replace("\"", "\"\"")}\",${t.plannedDate},${t.priority.name},${t.status.name},${t.estimatedMinutes},${t.actualMinutes},\"${t.completionNotes.replace("\"", "\"\"")}\"\n")
        }

        sb.append("\n--- FOCUS SESSIONS ---\n")
        sb.append("ID,TaskId,StartedAt,FocusedSeconds,FocusedMinutes,Status,Interruption\n")
        for (s in sessions) {
            sb.append("${s.id},${s.taskId ?: ""},${DateUtils.dateFromMillis(s.startedAt)},${s.focusedSeconds},${s.focusedSeconds / 60},${s.status},\"${(s.interruptionNote ?: "").replace("\"", "\"\"")}\"\n")
        }

        sb.append("\n--- DISTRACTIONS ---\n")
        sb.append("ID,Category,DurationMinutes,OccurredAt,Note\n")
        for (d in distractions) {
            sb.append("${d.id},\"${d.category}\",${d.durationMinutes},${DateUtils.dateFromMillis(d.occurredAt)},\"${d.note.replace("\"", "\"\"")}\"\n")
        }

        sb.append("\n--- DAILY REVIEWS ---\n")
        sb.append("Date,EnergyRating,FocusRating,WhatWentWell,MainDifficulty,LessonLearned,TomorrowPriority\n")
        for (r in reviews) {
            sb.append("${r.reviewDate},${r.energyRating},${r.focusRating},\"${r.whatWentWell.replace("\"", "\"\"")}\",\"${r.mainDifficulty.replace("\"", "\"\"")}\",\"${r.lessonLearned.replace("\"", "\"\"")}\",\"${r.tomorrowPriority.replace("\"", "\"\"")}\"\n")
        }

        sb.toString()
    }

    // SEED DEMO DATA
    suspend fun seedDemoData() = withContext(Dispatchers.IO) {
        val today = DateUtils.todayString()
        val yesterday = DateUtils.addDays(today, -1)
        val twoDaysAgo = DateUtils.addDays(today, -2)
        val threeDaysAgo = DateUtils.addDays(today, -3)
        val fourDaysAgo = DateUtils.addDays(today, -4)

        // Goals
        val g1Id = goalDao.insertGoal(
            Goal(
                title = "Ship Momentum Architecture",
                description = "Complete the core Jetpack Compose application architecture",
                category = "Coding",
                targetDate = DateUtils.addDays(today, 10),
                status = "ACTIVE"
            )
        )
        val g2Id = goalDao.insertGoal(
            Goal(
                title = "Master Systems Design & Algorithms",
                description = "Daily active recall and problem solving practice",
                category = "College",
                targetDate = DateUtils.addDays(today, 30),
                status = "ACTIVE"
            )
        )
        val g3Id = goalDao.insertGoal(
            Goal(
                title = "Consistent Exercise & Recovery",
                description = "Weekly workout consistency and energy management",
                category = "Fitness",
                targetDate = DateUtils.addDays(today, 60),
                status = "ACTIVE"
            )
        )

        // Tasks for today
        taskDao.insertTask(
            TaskItem(
                goalId = g1Id,
                title = "Implement Focus Timer & Session Recovery",
                description = "Ensure state persists cleanly across app lifecycles",
                plannedDate = today,
                estimatedMinutes = 45,
                actualMinutes = 40,
                priority = Priority.HIGH,
                status = TaskStatus.COMPLETED,
                completionNotes = "Finished Room DAO and reactive Flow state management"
            )
        )
        taskDao.insertTask(
            TaskItem(
                goalId = g2Id,
                title = "Review Distributed Transactions & Raft Consensus",
                description = "Self-quiz on leader election and log replication",
                plannedDate = today,
                estimatedMinutes = 30,
                actualMinutes = 0,
                priority = Priority.HIGH,
                status = TaskStatus.PLANNED
            )
        )
        taskDao.insertTask(
            TaskItem(
                goalId = g3Id,
                title = "45-minute Zone 2 Cardio & Mobility",
                description = "Outdoor run or rowing machine session",
                plannedDate = today,
                estimatedMinutes = 45,
                actualMinutes = 0,
                priority = Priority.MEDIUM,
                status = TaskStatus.PLANNED
            )
        )

        // Past Tasks with realistic variance
        taskDao.insertTask(
            TaskItem(
                goalId = g1Id,
                title = "Draft Database Schema & Room Entities",
                description = "All tables normalized with foreign keys",
                plannedDate = yesterday,
                estimatedMinutes = 40,
                actualMinutes = 55,
                priority = Priority.HIGH,
                status = TaskStatus.COMPLETED,
                completionNotes = "Took longer due to foreign key indexing verification"
            )
        )
        taskDao.insertTask(
            TaskItem(
                goalId = g2Id,
                title = "Solve 2 Graph Algorithms Problems",
                description = "Dijkstra and Topological Sort implementation",
                plannedDate = yesterday,
                estimatedMinutes = 35,
                actualMinutes = 50,
                priority = Priority.HIGH,
                status = TaskStatus.COMPLETED,
                completionNotes = "Topological sort cycle detection required extra test cases"
            )
        )
        taskDao.insertTask(
            TaskItem(
                goalId = g1Id,
                title = "Compose Analytics Service & Rule Engine",
                description = "Write deterministic mathematical formulas",
                plannedDate = twoDaysAgo,
                estimatedMinutes = 45,
                actualMinutes = 65,
                priority = Priority.HIGH,
                status = TaskStatus.COMPLETED,
                completionNotes = "Verified denominator safety"
            )
        )
        taskDao.insertTask(
            TaskItem(
                goalId = g2Id,
                title = "Operating Systems: Virtual Memory Paging",
                description = "TLB misses and page replacement algorithms",
                plannedDate = threeDaysAgo,
                estimatedMinutes = 30,
                actualMinutes = 35,
                priority = Priority.MEDIUM,
                status = TaskStatus.COMPLETED
            )
        )

        // Focus Sessions
        val now = System.currentTimeMillis()
        focusSessionDao.insertSession(
            FocusSession(
                taskId = null,
                startedAt = now - 3600000,
                endedAt = now - 1200000,
                focusedSeconds = 2400, // 40 mins
                status = SessionStatus.COMPLETED.name
            )
        )
        focusSessionDao.insertSession(
            FocusSession(
                taskId = null,
                startedAt = now - 86400000 - 7200000,
                endedAt = now - 86400000 - 3900000,
                focusedSeconds = 3300, // 55 mins
                status = SessionStatus.COMPLETED.name
            )
        )
        focusSessionDao.insertSession(
            FocusSession(
                taskId = null,
                startedAt = now - 172800000 - 5400000,
                endedAt = now - 172800000 - 1500000,
                focusedSeconds = 3900, // 65 mins
                status = SessionStatus.COMPLETED.name
            )
        )
        focusSessionDao.insertSession(
            FocusSession(
                taskId = null,
                startedAt = now - 259200000 - 3600000,
                endedAt = now - 259200000 - 1800000,
                focusedSeconds = 1800, // 30 mins
                status = SessionStatus.COMPLETED.name
            )
        )

        // Distractions
        distractionDao.insertDistraction(
            Distraction(
                category = "phone_use",
                durationMinutes = 12,
                note = "Checked social notifications during break and got pulled in",
                occurredAt = now - 86400000 + 1800000
            )
        )
        distractionDao.insertDistraction(
            Distraction(
                category = "phone_use",
                durationMinutes = 15,
                note = "Watched video clips while waiting for build",
                occurredAt = now - 172800000 + 3600000
            )
        )
        distractionDao.insertDistraction(
            Distraction(
                category = "notifications",
                durationMinutes = 8,
                note = "Group chat messages buzzing continuously",
                occurredAt = now - 259200000 + 7200000
            )
        )

        // Learning Topics & Revision Logs
        val top1Id = learningTopicDao.insertTopic(
            LearningTopic(
                title = "Raft Distributed Consensus",
                subject = "Distributed Systems",
                goalId = g2Id,
                initialStudyDate = fourDaysAgo,
                nextRevisionDate = today,
                currentIntervalIndex = 1
            )
        )
        val top2Id = learningTopicDao.insertTopic(
            LearningTopic(
                title = "Jetpack Compose Recomposition Optimization",
                subject = "Android Architecture",
                goalId = g1Id,
                initialStudyDate = twoDaysAgo,
                nextRevisionDate = DateUtils.addDays(today, 2),
                currentIntervalIndex = 0
            )
        )
        val top3Id = learningTopicDao.insertTopic(
            LearningTopic(
                title = "B-Tree vs LSM-Tree Indexing",
                subject = "Database Internals",
                goalId = g2Id,
                initialStudyDate = threeDaysAgo,
                nextRevisionDate = today,
                currentIntervalIndex = 0
            )
        )

        revisionLogDao.insertLog(
            RevisionLog(
                learningTopicId = top1Id,
                dueDate = yesterday,
                completedAt = now - 86400000,
                recallRating = RecallRating.RECALLED_SUCCESSFULLY.name,
                notes = "Recalled leader election term transitions clearly."
            )
        )
        revisionLogDao.insertLog(
            RevisionLog(
                learningTopicId = top2Id,
                dueDate = twoDaysAgo,
                completedAt = now - 172800000,
                recallRating = RecallRating.RECALLED_WITH_HINTS.name,
                notes = "Forgot stability markers annotation details."
            )
        )

        // Daily Reviews
        dailyReviewDao.insertOrUpdateReview(
            DailyReview(
                reviewDate = yesterday,
                energyRating = 4,
                focusRating = 4,
                whatWentWell = "Shipped the core Room database schema with clean foreign keys and indexes.",
                mainDifficulty = "Slight fatigue in the mid-afternoon; phone distraction interrupted Flow.",
                lessonLearned = "Leaving the phone in another room increases focus endurance significantly.",
                tomorrowPriority = "Implement reactive focus timer and analytics engine."
            )
        )

        // Experiments
        experimentDao.insertExperiment(
            ImprovementExperiment(
                title = "Phone in Drawer during 9-11 AM Deep Work",
                description = "Place smartphone in desk drawer on silent during morning focus sessions to cut phone distractions.",
                startDate = today,
                endDate = DateUtils.addDays(today, 7),
                status = "ACTIVE"
            )
        )
    }
}
