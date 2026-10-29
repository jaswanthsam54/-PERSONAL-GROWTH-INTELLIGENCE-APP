package com.example.service

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.FocusSession
import com.example.data.model.SessionStatus
import com.example.data.repository.MomentumRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Int = 0,
    val targetMinutes: Int = 25,
    val activeSessionId: Long? = null,
    val activeTaskId: Long? = null,
    val activeTaskTitle: String? = null
)

class FocusTimerManager(
    private val context: Context,
    private val repository: MomentumRepository
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("momentum_timer_prefs", Context.MODE_PRIVATE)

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: Job? = null

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    init {
        restoreState()
    }

    private fun restoreState() {
        val hasActive = prefs.getBoolean("has_active_session", false)
        if (!hasActive) {
            _timerState.value = TimerState()
            return
        }

        val isRunning = prefs.getBoolean("is_running", false)
        val isPaused = prefs.getBoolean("is_paused", false)
        val sessionId = prefs.getLong("session_id", -1L).takeIf { it != -1L }
        val taskId = prefs.getLong("task_id", -1L).takeIf { it != -1L }
        val taskTitle = prefs.getString("task_title", null)
        val targetMin = prefs.getInt("target_minutes", 25)
        val accumulatedSec = prefs.getInt("accumulated_seconds", 0)
        val segmentStartMs = prefs.getLong("segment_start_ms", 0L)

        var elapsed = accumulatedSec
        if (isRunning && segmentStartMs > 0) {
            val additionalSec = ((System.currentTimeMillis() - segmentStartMs) / 1000).toInt().coerceAtLeast(0)
            elapsed += additionalSec
        }

        _timerState.value = TimerState(
            isRunning = isRunning,
            isPaused = isPaused,
            elapsedSeconds = elapsed,
            targetMinutes = targetMin,
            activeSessionId = sessionId,
            activeTaskId = taskId,
            activeTaskTitle = taskTitle
        )

        if (isRunning) {
            startTicker()
        }
    }

    fun startSession(
        targetMinutes: Int = 25,
        taskId: Long? = null,
        taskTitle: String? = null
    ) {
        scope.launch {
            val now = System.currentTimeMillis()
            // Create database session record
            val sessionId = repository.insertSession(
                FocusSession(
                    taskId = taskId,
                    startedAt = now,
                    status = SessionStatus.ACTIVE.name,
                    focusedSeconds = 0
                )
            )

            // Save in prefs
            prefs.edit()
                .putBoolean("has_active_session", true)
                .putBoolean("is_running", true)
                .putBoolean("is_paused", false)
                .putLong("session_id", sessionId)
                .putLong("task_id", taskId ?: -1L)
                .putString("task_title", taskTitle)
                .putInt("target_minutes", targetMinutes)
                .putInt("accumulated_seconds", 0)
                .putLong("segment_start_ms", now)
                .apply()

            _timerState.value = TimerState(
                isRunning = true,
                isPaused = false,
                elapsedSeconds = 0,
                targetMinutes = targetMinutes,
                activeSessionId = sessionId,
                activeTaskId = taskId,
                activeTaskTitle = taskTitle
            )

            startTicker()
        }
    }

    fun pauseSession() {
        val current = _timerState.value
        if (!current.isRunning) return

        tickerJob?.cancel()
        val now = System.currentTimeMillis()
        val segmentStartMs = prefs.getLong("segment_start_ms", now)
        val accumulatedSec = prefs.getInt("accumulated_seconds", 0)
        val segmentSec = ((now - segmentStartMs) / 1000).toInt().coerceAtLeast(0)
        val newAccumulated = accumulatedSec + segmentSec

        prefs.edit()
            .putBoolean("is_running", false)
            .putBoolean("is_paused", true)
            .putInt("accumulated_seconds", newAccumulated)
            .putLong("segment_start_ms", 0L)
            .apply()

        _timerState.value = current.copy(
            isRunning = false,
            isPaused = true,
            elapsedSeconds = newAccumulated
        )

        scope.launch {
            current.activeSessionId?.let { sId ->
                repository.updateSession(
                    FocusSession(
                        id = sId,
                        taskId = current.activeTaskId,
                        status = SessionStatus.PAUSED.name,
                        focusedSeconds = newAccumulated
                    )
                )
            }
        }
    }

    fun resumeSession() {
        val current = _timerState.value
        if (!current.isPaused) return

        val now = System.currentTimeMillis()
        prefs.edit()
            .putBoolean("is_running", true)
            .putBoolean("is_paused", false)
            .putLong("segment_start_ms", now)
            .apply()

        _timerState.value = current.copy(
            isRunning = true,
            isPaused = false
        )

        scope.launch {
            current.activeSessionId?.let { sId ->
                repository.updateSession(
                    FocusSession(
                        id = sId,
                        taskId = current.activeTaskId,
                        status = SessionStatus.ACTIVE.name,
                        focusedSeconds = current.elapsedSeconds
                    )
                )
            }
        }

        startTicker()
    }

    fun finishSession(interruptionNote: String? = null) {
        val current = _timerState.value
        tickerJob?.cancel()

        val now = System.currentTimeMillis()
        val segmentStartMs = prefs.getLong("segment_start_ms", now)
        val accumulatedSec = prefs.getInt("accumulated_seconds", 0)
        val segmentSec = if (current.isRunning && segmentStartMs > 0) {
            ((now - segmentStartMs) / 1000).toInt().coerceAtLeast(0)
        } else 0
        val finalSec = accumulatedSec + segmentSec

        scope.launch {
            current.activeSessionId?.let { sId ->
                repository.updateSession(
                    FocusSession(
                        id = sId,
                        taskId = current.activeTaskId,
                        endedAt = now,
                        focusedSeconds = finalSec,
                        status = SessionStatus.COMPLETED.name,
                        interruptionNote = interruptionNote
                    )
                )
            }

            // Update associated task's actual minutes
            current.activeTaskId?.let { tId ->
                val task = repository.getTaskById(tId)
                if (task != null) {
                    val additionalMinutes = (finalSec / 60).coerceAtLeast(1)
                    val newActual = task.actualMinutes + additionalMinutes
                    repository.updateTask(task.copy(actualMinutes = newActual))
                }
            }

            clearPrefs()
            _timerState.value = TimerState()
        }
    }

    fun cancelSession() {
        val current = _timerState.value
        tickerJob?.cancel()

        scope.launch {
            current.activeSessionId?.let { sId ->
                repository.deleteSession(FocusSession(id = sId))
            }
            clearPrefs()
            _timerState.value = TimerState()
        }
    }

    private fun clearPrefs() {
        prefs.edit()
            .putBoolean("has_active_session", false)
            .putBoolean("is_running", false)
            .putBoolean("is_paused", false)
            .remove("session_id")
            .remove("task_id")
            .remove("task_title")
            .putInt("accumulated_seconds", 0)
            .putLong("segment_start_ms", 0L)
            .apply()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                delay(1000L)
                val now = System.currentTimeMillis()
                val segmentStartMs = prefs.getLong("segment_start_ms", now)
                val accumulatedSec = prefs.getInt("accumulated_seconds", 0)
                val segmentSec = if (segmentStartMs > 0) {
                    ((now - segmentStartMs) / 1000).toInt().coerceAtLeast(0)
                } else 0

                val current = _timerState.value
                _timerState.value = current.copy(
                    elapsedSeconds = accumulatedSec + segmentSec
                )
            }
        }
    }
}
