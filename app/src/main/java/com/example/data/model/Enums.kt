package com.example.data.model

enum class Priority(val label: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3);

    companion object {
        fun fromString(value: String): Priority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}

enum class TaskStatus(val label: String) {
    PLANNED("Planned"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    PARTIALLY_COMPLETED("Partially Completed"),
    SKIPPED("Skipped");

    companion object {
        fun fromString(value: String): TaskStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PLANNED
        }
    }
}

enum class SessionStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    CANCELLED
}

enum class RecallRating(val label: String, val scoreMultiplier: Float) {
    RECALLED_SUCCESSFULLY("Recalled Successfully", 1.0f),
    RECALLED_WITH_HINTS("Recalled with Hints", 0.5f),
    NEEDED_RELEARNING("Needed Relearning", 0.0f);

    companion object {
        fun fromString(value: String): RecallRating {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: RECALLED_SUCCESSFULLY
        }
    }
}

enum class ExperimentStatus(val label: String) {
    PROPOSED("Proposed"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    DISMISSED("Dismissed")
}
