package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goals",
    indices = [Index(value = ["status"]), Index(value = ["category"])]
)
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "General",
    @ColumnInfo(name = "target_date")
    val targetDate: String = "",
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, ARCHIVED
    @ColumnInfo(name = "target_metric")
    val targetMetric: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["goal_id"]),
        Index(value = ["planned_date"]),
        Index(value = ["status"]),
        Index(value = ["priority"])
    ]
)
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "goal_id")
    val goalId: Long? = null,
    val title: String,
    val description: String = "",
    @ColumnInfo(name = "planned_date")
    val plannedDate: String, // YYYY-MM-DD
    @ColumnInfo(name = "estimated_minutes")
    val estimatedMinutes: Int = 30,
    @ColumnInfo(name = "actual_minutes")
    val actualMinutes: Int = 0,
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.PLANNED,
    @ColumnInfo(name = "completion_notes")
    val completionNotes: String = "",
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "focus_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TaskItem::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["task_id"]),
        Index(value = ["started_at"]),
        Index(value = ["status"])
    ]
)
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "task_id")
    val taskId: Long? = null,
    @ColumnInfo(name = "started_at")
    val startedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "ended_at")
    val endedAt: Long? = null,
    @ColumnInfo(name = "focused_seconds")
    val focusedSeconds: Int = 0,
    val status: String = SessionStatus.COMPLETED.name,
    @ColumnInfo(name = "interruption_note")
    val interruptionNote: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "distractions",
    foreignKeys = [
        ForeignKey(
            entity = TaskItem::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = FocusSession::class,
            parentColumns = ["id"],
            childColumns = ["focus_session_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["task_id"]),
        Index(value = ["focus_session_id"]),
        Index(value = ["occurred_at"]),
        Index(value = ["category"])
    ]
)
data class Distraction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "task_id")
    val taskId: Long? = null,
    @ColumnInfo(name = "focus_session_id")
    val focusSessionId: Long? = null,
    val category: String, // phone use, unclear task, notifications, fatigue, environment, other
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int = 5,
    val note: String = "",
    @ColumnInfo(name = "occurred_at")
    val occurredAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "learning_topics",
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["goal_id"]),
        Index(value = ["next_revision_date"]),
        Index(value = ["subject"])
    ]
)
data class LearningTopic(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subject: String = "General",
    @ColumnInfo(name = "goal_id")
    val goalId: Long? = null,
    @ColumnInfo(name = "initial_study_date")
    val initialStudyDate: String, // YYYY-MM-DD
    @ColumnInfo(name = "next_revision_date")
    val nextRevisionDate: String, // YYYY-MM-DD
    @ColumnInfo(name = "current_interval_index")
    val currentIntervalIndex: Int = 0, // 0 -> 1 day, 1 -> 4 days, 2 -> 7 days, 3 -> mastered/14d
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "revision_logs",
    foreignKeys = [
        ForeignKey(
            entity = LearningTopic::class,
            parentColumns = ["id"],
            childColumns = ["learning_topic_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["learning_topic_id"]),
        Index(value = ["due_date"]),
        Index(value = ["completed_at"])
    ]
)
data class RevisionLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "learning_topic_id")
    val learningTopicId: Long,
    @ColumnInfo(name = "due_date")
    val dueDate: String, // YYYY-MM-DD
    @ColumnInfo(name = "completed_at")
    val completedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "recall_rating")
    val recallRating: String, // RECALLED_SUCCESSFULLY, RECALLED_WITH_HINTS, NEEDED_RELEARNING
    val notes: String = ""
)

@Entity(
    tableName = "daily_reviews",
    indices = [Index(value = ["review_date"], unique = true)]
)
data class DailyReview(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "review_date")
    val reviewDate: String, // YYYY-MM-DD
    @ColumnInfo(name = "energy_rating")
    val energyRating: Int = 3, // 1..5
    @ColumnInfo(name = "focus_rating")
    val focusRating: Int = 3, // 1..5
    @ColumnInfo(name = "what_went_well")
    val whatWentWell: String = "",
    @ColumnInfo(name = "main_difficulty")
    val mainDifficulty: String = "",
    @ColumnInfo(name = "lesson_learned")
    val lessonLearned: String = "",
    @ColumnInfo(name = "tomorrow_priority")
    val tomorrowPriority: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "improvement_experiments",
    indices = [Index(value = ["status"]), Index(value = ["start_date"])]
)
data class ImprovementExperiment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    @ColumnInfo(name = "start_date")
    val startDate: String, // YYYY-MM-DD
    @ColumnInfo(name = "end_date")
    val endDate: String, // YYYY-MM-DD
    val status: String = "ACTIVE", // PROPOSED, ACTIVE, COMPLETED, DISMISSED
    @ColumnInfo(name = "outcome_notes")
    val outcomeNotes: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
