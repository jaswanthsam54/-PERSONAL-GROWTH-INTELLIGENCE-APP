package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY updated_at DESC")
    fun getAllGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE status = :status ORDER BY updated_at DESC")
    fun getGoalsByStatus(status: String): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :id LIMIT 1")
    suspend fun getGoalById(id: Long): Goal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    @Query("DELETE FROM goals")
    suspend fun clearAll()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY planned_date ASC, priority DESC, created_at DESC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE planned_date = :date ORDER BY priority DESC, created_at ASC")
    fun getTasksForDate(date: String): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE planned_date < :date AND status != 'COMPLETED' AND status != 'SKIPPED' ORDER BY planned_date ASC, priority DESC")
    fun getUnfinishedPastTasks(date: String): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE goal_id = :goalId ORDER BY planned_date ASC")
    fun getTasksForGoal(goalId: Long): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): TaskItem?

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksDirect(): List<TaskItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun clearAll()
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY started_at DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE started_at >= :startMillis AND started_at <= :endMillis ORDER BY started_at DESC")
    fun getSessionsInRange(startMillis: Long, endMillis: Long): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE status = 'ACTIVE' OR status = 'PAUSED' ORDER BY started_at DESC LIMIT 1")
    fun getActiveSessionFlow(): Flow<FocusSession?>

    @Query("SELECT * FROM focus_sessions WHERE status = 'ACTIVE' OR status = 'PAUSED' ORDER BY started_at DESC LIMIT 1")
    suspend fun getActiveSession(): FocusSession?

    @Query("SELECT * FROM focus_sessions")
    suspend fun getAllSessionsDirect(): List<FocusSession>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession): Long

    @Update
    suspend fun updateSession(session: FocusSession)

    @Delete
    suspend fun deleteSession(session: FocusSession)

    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAll()
}

@Dao
interface DistractionDao {
    @Query("SELECT * FROM distractions ORDER BY occurred_at DESC")
    fun getAllDistractions(): Flow<List<Distraction>>

    @Query("SELECT * FROM distractions WHERE occurred_at >= :startMillis AND occurred_at <= :endMillis ORDER BY occurred_at DESC")
    fun getDistractionsInRange(startMillis: Long, endMillis: Long): Flow<List<Distraction>>

    @Query("SELECT * FROM distractions")
    suspend fun getAllDistractionsDirect(): List<Distraction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDistraction(distraction: Distraction): Long

    @Delete
    suspend fun deleteDistraction(distraction: Distraction)

    @Query("DELETE FROM distractions WHERE id = :id")
    suspend fun deleteDistractionById(id: Long)

    @Query("DELETE FROM distractions")
    suspend fun clearAll()
}

@Dao
interface LearningTopicDao {
    @Query("SELECT * FROM learning_topics ORDER BY next_revision_date ASC, updated_at DESC")
    fun getAllTopics(): Flow<List<LearningTopic>>

    @Query("SELECT * FROM learning_topics WHERE next_revision_date <= :date ORDER BY next_revision_date ASC")
    fun getDueRevisions(date: String): Flow<List<LearningTopic>>

    @Query("SELECT * FROM learning_topics WHERE next_revision_date > :date ORDER BY next_revision_date ASC")
    fun getUpcomingRevisions(date: String): Flow<List<LearningTopic>>

    @Query("SELECT * FROM learning_topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: Long): LearningTopic?

    @Query("SELECT * FROM learning_topics")
    suspend fun getAllTopicsDirect(): List<LearningTopic>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: LearningTopic): Long

    @Update
    suspend fun updateTopic(topic: LearningTopic)

    @Delete
    suspend fun deleteTopic(topic: LearningTopic)

    @Query("DELETE FROM learning_topics WHERE id = :id")
    suspend fun deleteTopicById(id: Long)

    @Query("DELETE FROM learning_topics")
    suspend fun clearAll()
}

@Dao
interface RevisionLogDao {
    @Query("SELECT * FROM revision_logs ORDER BY completed_at DESC")
    fun getAllLogs(): Flow<List<RevisionLog>>

    @Query("SELECT * FROM revision_logs WHERE learning_topic_id = :topicId ORDER BY completed_at DESC")
    fun getLogsForTopic(topicId: Long): Flow<List<RevisionLog>>

    @Query("SELECT * FROM revision_logs")
    suspend fun getAllLogsDirect(): List<RevisionLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: RevisionLog): Long

    @Delete
    suspend fun deleteLog(log: RevisionLog)

    @Query("DELETE FROM revision_logs")
    suspend fun clearAll()
}

@Dao
interface DailyReviewDao {
    @Query("SELECT * FROM daily_reviews ORDER BY review_date DESC")
    fun getAllReviews(): Flow<List<DailyReview>>

    @Query("SELECT * FROM daily_reviews WHERE review_date = :date LIMIT 1")
    fun getReviewForDateFlow(date: String): Flow<DailyReview?>

    @Query("SELECT * FROM daily_reviews WHERE review_date = :date LIMIT 1")
    suspend fun getReviewForDate(date: String): DailyReview?

    @Query("SELECT * FROM daily_reviews")
    suspend fun getAllReviewsDirect(): List<DailyReview>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReview(review: DailyReview): Long

    @Delete
    suspend fun deleteReview(review: DailyReview)

    @Query("DELETE FROM daily_reviews")
    suspend fun clearAll()
}

@Dao
interface ExperimentDao {
    @Query("SELECT * FROM improvement_experiments ORDER BY created_at DESC")
    fun getAllExperiments(): Flow<List<ImprovementExperiment>>

    @Query("SELECT * FROM improvement_experiments WHERE status = :status ORDER BY created_at DESC")
    fun getExperimentsByStatus(status: String): Flow<List<ImprovementExperiment>>

    @Query("SELECT * FROM improvement_experiments WHERE id = :id LIMIT 1")
    suspend fun getExperimentById(id: Long): ImprovementExperiment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperiment(experiment: ImprovementExperiment): Long

    @Update
    suspend fun updateExperiment(experiment: ImprovementExperiment)

    @Delete
    suspend fun deleteExperiment(experiment: ImprovementExperiment)

    @Query("DELETE FROM improvement_experiments")
    suspend fun clearAll()
}
