# Momentum — Personal Growth Intelligence System

**Momentum** is a local-first personal productivity and growth intelligence system built for Android with Kotlin and Jetpack Compose (Material 3).

Unlike generic to-do apps or habit checklists, Momentum operates on a closed feedback loop:
**PLAN → EXECUTE → RECORD → ANALYZE → IMPROVE**

All personal analytics, estimation error margins, distraction breakdowns, and growth insights are derived strictly from genuine records entered through the user's daily actions.

---

## Core Features

### 1. Home Dashboard
- Daily personalized overview with the current date and progress motto.
- Planned vs. completed tasks count with real-time percentage indicators.
- Actual focused minutes recorded today.
- Today's top high priorities with quick 1-tap completion and direct focus session launcher.
- Daily energy rating indicator from evening reflections.
- Spaced revisions due alert badge.
- Weekly focus minutes bar visualization.
- Quick action shortcuts: Start Focus, Plan Task, Weekly Retrospective.

### 2. Task Planner
- Daily task scheduling with simple day-to-day navigation (Yesterday, Today, Tomorrow, Custom dates).
- Multi-dimensional task attributes: title, description, planned date, estimated minutes, actual minutes, priority (High, Medium, Low), status (Planned, In Progress, Completed, Partially Completed, Skipped), completion notes, and associated goal.
- Unfinished past tasks alert with 1-tap "Move to Today" rescheduling.
- Real-time filtering by status and priority.
- Completion dialog for capturing actual duration and retrospective notes.

### 3. Focus Engine & Session Timer
- Timestamp-based timer ensuring 100% accurate tracking resilient to device reboots, app kills, and background transitions.
- Does not count paused intervals as focused time.
- Task association: link focus blocks directly to scheduled priorities to automatically update task execution time.
- Interruption / Distraction logging: record phone use, unclear task scopes, notifications, fatigue, or environment friction directly from the timer.
- Floating persistent timer banner displayed across all screens whenever a focus session is active or paused.
- History log of completed sessions with durations and interruption notes.

### 4. Spaced Revision Tracker (Learning Retention)
- Spaced repetition engine with automated Leitner interval progression:
  - Day 1 → Day 4 → Day 7 → Day 14+
- Active recall rating on test attempts:
  - **Recalled Successfully**: Advances to the next spaced interval.
  - **Recalled with Hints**: Repeats in 2 days to reinforce retention.
  - **Needed Relearning**: Resets interval back to Day 1.
- Tracks subject-level recall accuracy.

### 5. Distraction Journal
- Categorized friction logging: Phone Use, Notifications, Unclear Task, Fatigue, Environment, and Custom.
- Frequency and duration breakdown with percentage distribution progress bars.
- Helps identify primary friction triggers that steal focus endurance.

### 6. Analytics & Personal Insights Engine
- **Deterministic, rule-based intelligence**: No hallucinating language models or fake sample statistics.
- **Priority Completion Rate**: `(Completed high priorities / Total high priorities) * 100` (handles zero denominators gracefully without false 0% reporting).
- **Time Estimation Error**: `Actual duration - Estimated duration` across completed tasks to detect chronic underestimation or overestimation.
- **Revision Recall Rate**: `(Successful recall attempts / Total completed attempts) * 100`.
- **Week-over-Week Comparison**: Focus minutes delta and high-priority execution rates compared with the previous week.
- **28-Day Activity Heatmap**: Visual intensity grid mapping finished tasks and focus sessions.
- **Evidence-Based Insight Cards**: Each insight includes observation, period analyzed, supporting metrics, and practical suggested experiments.

### 7. Review & Retrospectives
- **Daily Reflection**: Energy level (1-5 stars), focus clarity (1-5 stars), what went well, main difficulty, lesson learned, and tomorrow's #1 priority.
- **Weekly Review**: Retrospective metrics summary, suggested improvement experiments (e.g. "Phone in Drawer during 9-11 AM", "25% Estimation Buffer"), and custom experiment tracker with outcome logs.

### 8. Settings & Data Portability
- **100% Local-First**: SQLite database stored locally via Android Jetpack Room. No remote servers or required account logins.
- **CSV Data Export**: 1-tap export copying structured CSV tables (tasks, focus sessions, distractions, reviews) directly to clipboard.
- **Test Drive Demo Data**: 1-click generator to populate realistic historical records for testing all analytics and charts immediately.
- **Clear All Data**: Irreversible database reset with explicit confirmation.

---

## Architecture & Technology Stack

- **Platform:** Android (Min SDK 24, Target SDK 36)
- **Language:** Kotlin 2.2.10
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Local Database:** Android Jetpack Room 2.7.0 (SQLite with KSP compiler, foreign keys, and indexes)
- **Concurrency & Reactivity:** Kotlin Coroutines & `Flow` / `StateFlow`
- **Architecture Pattern:** MVVM (Model - View - ViewModel) + Repository Pattern
- **Testing:** JUnit 4 + Robolectric (JVM-based testing for fast local verification)

### Project Directory Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                # Main Android activity with edge-to-edge Compose entry point
├── core/
│   └── util/
│       └── DateUtils.kt           # Date conversions, ISO formatters, week calculations
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt         # Room SQLite database configuration
│   │   └── Daos.kt                # Reactive DAO interfaces with Flow queries
│   ├── model/
│   │   ├── Entities.kt            # Room entities (Goal, TaskItem, FocusSession, Distraction, etc.)
│   │   └── Enums.kt               # Priority, TaskStatus, SessionStatus, RecallRating
│   └── repository/
│       └── MomentumRepository.kt  # Repository layer with CSV export & demo seeder
├── service/
│   ├── AnalyticsService.kt        # Deterministic productivity calculations & safe denominators
│   ├── FocusTimerManager.kt       # Timestamp-accurate timer state manager with restart persistence
│   └── InsightsEngine.kt          # Rule-based personal growth observations & action recommendations
└── ui/
    ├── MomentumApp.kt             # Root scaffold, bottom navigation, floating timer banner
    ├── MomentumViewModel.kt       # Central ViewModel exposing StateFlows & user actions
    ├── components/
    │   ├── AddTaskDialog.kt       # Task creation dialog
    │   └── MomentumComponents.kt  # Badges, StatCards, FocusBarChart, ActivityHeatmap
    ├── screens/
    │   ├── DashboardScreen.kt     # Daily overview & top priorities
    │   ├── PlannerScreen.kt       # Daily scheduler & unfinished past tasks
    │   ├── FocusScreen.kt         # Focus session timer & distraction logging
    │   ├── InsightsScreen.kt      # Analytics metrics, heatmap, & personal insights cards
    │   ├── ReviewScreen.kt        # Daily reflection & weekly experiments
    │   ├── GoalsScreen.kt         # Goal management & linked task progress
    │   ├── RevisionScreen.kt      # Spaced revision Leitner tracker
    │   ├── DistractionsScreen.kt  # Distraction journal
    │   └── SettingsScreen.kt      # Data export & demo data options
    └── theme/
        ├── Color.kt               # Semantic palette (MomentumIndigo, MomentumGreen, Slate)
        └── Theme.kt               # M3 MomentumTheme (light & dark mode)
```

---

## Instructions for Running

### In AI Studio / Web Streaming Emulator
1. The app compiles and installs automatically in the cloud environment.
2. The Streaming Android Emulator displays the live interactive app directly in your browser.
3. Use the bottom navigation bar to navigate between **Home**, **Planner**, **Focus**, **Insights**, and **Review**.
4. To test-drive with populated analytics and charts immediately, open **Settings** (top right icon) and tap **"Load Realistic Demo Records"**.

### On Physical Android Device or Android Studio Emulator
1. Clone or export the project ZIP from the AI Studio settings menu.
2. Open the project folder in **Android Studio Ladybug (or newer)**.
3. Allow Gradle to sync dependencies (`Room`, `Compose`, `KSP`).
4. Select your connected Android phone or AVD emulator (Android 7.0 / API 24 or newer).
5. Click **Run** (`Shift + F10`) or run from terminal:
   ```bash
   gradle installDebug
   ```

### Running Automated Tests
Run local unit and Robolectric tests via Gradle:
```bash
gradle :app:testDebugUnitTest
```
