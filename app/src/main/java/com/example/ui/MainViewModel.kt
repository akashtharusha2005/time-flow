package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiRepository
import com.example.data.local.AppLanguage
import com.example.data.local.AppPreferences
import com.example.data.local.PreferencesManager
import com.example.data.local.ThemeMode
import com.example.data.local.TimeflowDatabase
import com.example.data.model.AiMessage
import com.example.data.model.CalendarEvent
import com.example.data.model.FocusRecord
import com.example.data.model.FocusSessionType
import com.example.data.model.GoalItem
import com.example.data.model.HabitItem
import com.example.data.model.NoteItem
import com.example.data.model.Priority
import com.example.data.model.StudySubject
import com.example.data.model.StudyTopic
import com.example.data.model.Subtask
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.UserSession
import com.example.data.repository.CalendarRepository
import com.example.data.repository.FocusRepository
import com.example.data.repository.HabitGoalRepository
import com.example.data.repository.NoteRepository
import com.example.data.repository.StudyRepository
import com.example.data.repository.TaskRepository
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class MainDestination {
    HOME, TASKS, CALENDAR, FOCUS, PROFILE, GOALS, HABITS, STUDY, ANALYTICS, NOTES, AI, SETTINGS
}

enum class TaskSortOption {
    DUE_DATE, PRIORITY, CREATED, TITLE
}

data class FocusTimerState(
    val isRunning: Boolean = false,
    val timeRemainingSeconds: Int = 25 * 60,
    val totalDurationSeconds: Int = 25 * 60,
    val currentType: FocusSessionType = FocusSessionType.POMODORO,
    val selectedTask: TaskItem? = null,
    val sessionsCompletedToday: Int = 0,
    val endTimestamp: Long? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val context = application.applicationContext
    private val taskRepository = TaskRepository(context)
    private val calendarRepository = CalendarRepository(context)
    private val focusRepository = FocusRepository(context)
    private val habitGoalRepository = HabitGoalRepository(context)
    private val studyRepository = StudyRepository(context)
    private val noteRepository = NoteRepository(context)
    private val preferencesManager = PreferencesManager(context)
    private val aiRepository = AiRepository()
    private val notificationHelper = NotificationHelper(context)

    // Current navigation destination
    private val _currentDestination = MutableStateFlow(MainDestination.HOME)
    val currentDestination: StateFlow<MainDestination> = _currentDestination.asStateFlow()

    // Active User
    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    // App Preferences
    val preferences: StateFlow<AppPreferences> = preferencesManager.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppPreferences()
    )

    // App Lock State
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    // Data streams
    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    private val _focusRecords = MutableStateFlow<List<FocusRecord>>(emptyList())
    val focusRecords: StateFlow<List<FocusRecord>> = _focusRecords.asStateFlow()

    private val _habits = MutableStateFlow<List<HabitItem>>(emptyList())
    val habits: StateFlow<List<HabitItem>> = _habits.asStateFlow()

    private val _goals = MutableStateFlow<List<GoalItem>>(emptyList())
    val goals: StateFlow<List<GoalItem>> = _goals.asStateFlow()

    private val _subjects = MutableStateFlow<List<StudySubject>>(emptyList())
    val subjects: StateFlow<List<StudySubject>> = _subjects.asStateFlow()

    private val _topics = MutableStateFlow<List<StudyTopic>>(emptyList())
    val topics: StateFlow<List<StudyTopic>> = _topics.asStateFlow()

    private val _notes = MutableStateFlow<List<NoteItem>>(emptyList())
    val notes: StateFlow<List<NoteItem>> = _notes.asStateFlow()

    // Filter & Search states for Tasks
    val taskSearchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow<TaskCategory?>(null)
    val selectedPriorityFilter = MutableStateFlow<Priority?>(null)
    val showCompletedOnly = MutableStateFlow(false)
    val taskSortOption = MutableStateFlow(TaskSortOption.DUE_DATE)

    // Focus Timer
    private val _timerState = MutableStateFlow(FocusTimerState())
    val timerState: StateFlow<FocusTimerState> = _timerState.asStateFlow()
    private var timerJob: Job? = null

    // AI Chat
    private val _aiMessages = MutableStateFlow<List<AiMessage>>(
        listOf(
            AiMessage(
                id = "welcome",
                isUser = false,
                content = "Hello! I am TIMEFLOW AI. How can I help you plan your schedule, optimize study sessions, or break down your tasks today?"
            )
        )
    )
    val aiMessages: StateFlow<List<AiMessage>> = _aiMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    val aiHighThinking = MutableStateFlow(false)

    fun setUser(user: UserSession?) {
        _userSession.value = user
        if (user != null) {
            observeUserData(user.uid)
            if (preferences.value.isAppLockEnabled && preferences.value.appLockPin.isNotBlank()) {
                _isAppLocked.value = true
            }
        }
    }

    private fun observeUserData(userId: String) {
        viewModelScope.launch {
            taskRepository.getTasks(userId).collect { list ->
                _tasks.value = list
                seedInitialDataIfEmpty(userId, list)
            }
        }
        viewModelScope.launch {
            calendarRepository.getEvents(userId).collect { _events.value = it }
        }
        viewModelScope.launch {
            focusRepository.getFocusRecords(userId).collect { _focusRecords.value = it }
        }
        viewModelScope.launch {
            habitGoalRepository.getHabits(userId).collect { _habits.value = it }
        }
        viewModelScope.launch {
            habitGoalRepository.getGoals(userId).collect { _goals.value = it }
        }
        viewModelScope.launch {
            studyRepository.getSubjects(userId).collect { _subjects.value = it }
        }
        viewModelScope.launch {
            studyRepository.getTopics(userId).collect { _topics.value = it }
        }
        viewModelScope.launch {
            noteRepository.getNotes(userId).collect { _notes.value = it }
        }
    }

    private fun seedInitialDataIfEmpty(userId: String, existingTasks: List<TaskItem>) {
        if (existingTasks.isNotEmpty()) return
        viewModelScope.launch {
            // Seed a starter task and sample habit to guide the user immediately
            val starterTask = TaskItem(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Welcome to TIMEFLOW! Explore your dashboard",
                description = "Check out your schedule, start a Pomodoro timer, or ask AI to build a study plan.",
                priority = Priority.HIGH,
                category = TaskCategory.PERSONAL,
                dueDateMillis = System.currentTimeMillis() + 86400000,
                subtasks = listOf(
                    Subtask(UUID.randomUUID().toString(), "Try starting a Focus session", false),
                    Subtask(UUID.randomUUID().toString(), "Create a new study subject", false),
                    Subtask(UUID.randomUUID().toString(), "Ask AI for daily productivity tips", false)
                )
            )
            taskRepository.saveTask(starterTask)

            val starterHabit = HabitItem(
                id = UUID.randomUUID().toString(),
                userId = userId,
                title = "Morning Planning & Review",
                description = "Review daily priorities and timeblocks for 5 minutes",
                currentStreak = 1,
                bestStreak = 1,
                completedDates = listOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            )
            habitGoalRepository.saveHabit(starterHabit)

            val starterSubject = StudySubject(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "Mathematics & Logic",
                nameSinhala = "ගණිතය සහ තර්ක ශාස්ත්‍රය",
                examDateMillis = System.currentTimeMillis() + (14L * 86400000L),
                targetHours = 20.0,
                completedHours = 4.5
            )
            studyRepository.saveSubject(starterSubject)
        }
    }

    fun navigateTo(destination: MainDestination) {
        _currentDestination.value = destination
    }

    // App Lock
    fun unlockApp(pin: String): Boolean {
        if (pin == preferences.value.appLockPin) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun unlockWithBiometrics() {
        _isAppLocked.value = false
    }

    fun setAppLock(enabled: Boolean, pin: String) {
        viewModelScope.launch {
            preferencesManager.setAppLock(enabled, pin)
            _isAppLocked.value = false
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { preferencesManager.setLanguage(language) }
    }

    // Task Actions
    fun saveTask(task: TaskItem) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            taskRepository.saveTask(task.copy(userId = uid))
        }
    }

    fun toggleTaskCompletion(task: TaskItem) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(task)
            notificationHelper.vibrateDevice(50)
        }
    }

    fun deleteTask(taskId: String) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            taskRepository.deleteTask(uid, taskId)
        }
    }

    fun toggleSubtask(task: TaskItem, subtaskId: String) {
        val updatedSubtasks = task.subtasks.map {
            if (it.id == subtaskId) it.copy(isCompleted = !it.isCompleted) else it
        }
        saveTask(task.copy(subtasks = updatedSubtasks))
    }

    // Calendar Actions
    fun saveEvent(event: CalendarEvent) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            calendarRepository.saveEvent(event.copy(userId = uid))
        }
    }

    fun deleteEvent(eventId: String) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            calendarRepository.deleteEvent(uid, eventId)
        }
    }

    fun checkScheduleConflicts(startTime: Long, endTime: Long, excludeId: String? = null): List<CalendarEvent> {
        return calendarRepository.checkConflicts(_events.value, startTime, endTime, excludeId)
    }

    // Focus Timer Actions
    fun selectFocusTask(task: TaskItem?) {
        _timerState.value = _timerState.value.copy(selectedTask = task)
    }

    fun setFocusSessionType(type: FocusSessionType) {
        val minutes = when (type) {
            FocusSessionType.POMODORO -> preferences.value.pomodoroMinutes
            FocusSessionType.SHORT_BREAK -> preferences.value.shortBreakMinutes
            FocusSessionType.LONG_BREAK -> preferences.value.longBreakMinutes
        }
        val totalSecs = minutes * 60
        _timerState.value = _timerState.value.copy(
            currentType = type,
            timeRemainingSeconds = totalSecs,
            totalDurationSeconds = totalSecs,
            isRunning = false,
            endTimestamp = null
        )
        timerJob?.cancel()
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return
        val currentRemaining = _timerState.value.timeRemainingSeconds
        val endAt = System.currentTimeMillis() + (currentRemaining * 1000L)

        _timerState.value = _timerState.value.copy(
            isRunning = true,
            endTimestamp = endAt
        )

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value.isRunning) {
                delay(1000)
                val targetEnd = _timerState.value.endTimestamp ?: break
                val now = System.currentTimeMillis()
                val remaining = ((targetEnd - now) / 1000L).toInt().coerceAtLeast(0)

                _timerState.value = _timerState.value.copy(timeRemainingSeconds = remaining)

                if (remaining <= 0) {
                    onTimerFinished()
                    break
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false, endTimestamp = null)
    }

    fun resetTimer() {
        timerJob?.cancel()
        val totalSecs = _timerState.value.totalDurationSeconds
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            timeRemainingSeconds = totalSecs,
            endTimestamp = null
        )
    }

    private fun onTimerFinished() {
        timerJob?.cancel()
        val current = _timerState.value
        val completedDuration = current.totalDurationSeconds / 60
        val uid = _userSession.value?.uid

        if (uid != null && current.currentType == FocusSessionType.POMODORO) {
            viewModelScope.launch {
                focusRepository.recordSession(
                    userId = uid,
                    taskId = current.selectedTask?.id,
                    taskTitle = current.selectedTask?.title,
                    type = current.currentType,
                    durationMinutes = completedDuration,
                    completedMinutes = completedDuration
                )
            }
        }

        notificationHelper.showFocusFinishedNotification(
            title = if (current.currentType == FocusSessionType.POMODORO) "Focus Complete! 🎉" else "Break Finished! ⏰",
            message = if (current.currentType == FocusSessionType.POMODORO) "Great job! Time to take a short break." else "Ready to dive back into your flow?"
        )

        val nextType = if (current.currentType == FocusSessionType.POMODORO) {
            FocusSessionType.SHORT_BREAK
        } else {
            FocusSessionType.POMODORO
        }
        setFocusSessionType(nextType)
    }

    // Habits & Goals Actions
    fun toggleHabitToday(habit: HabitItem) {
        viewModelScope.launch {
            habitGoalRepository.toggleHabitToday(habit)
            notificationHelper.vibrateDevice(50)
        }
    }

    fun saveHabit(habit: HabitItem) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            habitGoalRepository.saveHabit(habit.copy(userId = uid))
        }
    }

    fun deleteHabit(habitId: String) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            habitGoalRepository.deleteHabit(uid, habitId)
        }
    }

    fun saveGoal(goal: GoalItem) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            habitGoalRepository.saveGoal(goal.copy(userId = uid))
        }
    }

    fun updateGoalProgress(goal: GoalItem, delta: Double) {
        viewModelScope.launch {
            habitGoalRepository.updateGoalProgress(goal, delta)
        }
    }

    fun deleteGoal(goalId: String) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            habitGoalRepository.deleteGoal(uid, goalId)
        }
    }

    // Study Actions
    fun saveSubject(subject: StudySubject) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            studyRepository.saveSubject(subject.copy(userId = uid))
        }
    }

    fun deleteSubject(subjectId: String) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            studyRepository.deleteSubject(uid, subjectId)
        }
    }

    fun saveTopic(topic: StudyTopic) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            studyRepository.saveTopic(topic.copy(userId = uid))
        }
    }

    fun toggleTopic(topic: StudyTopic) {
        viewModelScope.launch {
            studyRepository.toggleTopicCompletion(topic)
        }
    }

    fun deleteTopic(topicId: String) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            studyRepository.deleteTopic(uid, topicId)
        }
    }

    fun logStudyHours(subject: StudySubject, hours: Double) {
        viewModelScope.launch {
            studyRepository.addStudyHours(subject, hours)
        }
    }

    // Note Actions
    fun saveNote(note: NoteItem) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            noteRepository.saveNote(note.copy(userId = uid))
        }
    }

    fun togglePinNote(note: NoteItem) {
        viewModelScope.launch {
            noteRepository.togglePinNote(note)
        }
    }

    fun deleteNote(noteId: String) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            noteRepository.deleteNote(uid, noteId)
        }
    }

    // AI Chat & Assistance Actions
    fun sendAiMessage(prompt: String) {
        if (prompt.isBlank() || _isAiThinking.value) return

        val userMsg = AiMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            content = prompt
        )
        val currentHistory = _aiMessages.value + userMsg
        _aiMessages.value = currentHistory
        _isAiThinking.value = true

        viewModelScope.launch {
            val result = aiRepository.generateResponse(
                prompt = prompt,
                history = currentHistory,
                useHighThinking = aiHighThinking.value,
                language = if (preferences.value.language == AppLanguage.SI) "si" else "en"
            )

            _isAiThinking.value = false
            val aiResponse = result.getOrElse { err ->
                "⚠️ " + (err.localizedMessage ?: "Unable to connect to AI. Please verify your internet and API key.")
            }
            _aiMessages.value = _aiMessages.value + AiMessage(
                id = UUID.randomUUID().toString(),
                isUser = false,
                content = aiResponse,
                thinking = if (aiHighThinking.value) "Analyzed with High Thinking Mode" else null
            )
        }
    }

    fun generateAiStudyPlan(subject: StudySubject) {
        val prompt = if (preferences.value.language == AppLanguage.SI) {
            "මම '${subject.name}' විෂය සඳහා අධ්‍යයන සැලැස්මක් සකස් කිරීමට කැමතියි. මගේ ඉලක්කය පැය ${subject.targetHours} සම්පූර්ණ කිරීමයි. කරුණාකර දින 7 ක කාලසටහනක් සහ ප්‍රධාන මාතෘකා 5 ක ලැයිස්තුවක් යෝජනා කරන්න."
        } else {
            "Create a structured 7-day study plan for '${subject.name}' with a target of ${subject.targetHours} hours. Include recommended daily revision timeblocks and top 5 milestone topics."
        }
        navigateTo(MainDestination.AI)
        sendAiMessage(prompt)
    }

    fun generateAiDailySchedule() {
        val pendingCount = _tasks.value.filter { !it.isCompleted }.size
        val prompt = if (preferences.value.language == AppLanguage.SI) {
            "මට අද දින සඳහා ප්‍රශස්ත දෛනික කාලසටහනක් (Time Blocking) සාදා දෙන්න. මට අවසන් කිරීමට කාර්යයන් $pendingCount ක් ඇත. විවේක සහ අවධානය යොමු කළ කාලය ඇතුළත් කරන්න."
        } else {
            "Please create an optimized time-blocked daily planner schedule for today. I have $pendingCount pending tasks. Suggest timeblocks between 8:00 AM and 6:00 PM with Pomodoro focus intervals and break suggestions."
        }
        navigateTo(MainDestination.AI)
        sendAiMessage(prompt)
    }

    // Data Export
    fun exportUserDataJson(): String {
        val root = JSONObject()
        root.put("appName", "TIMEFLOW")
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        root.put("userId", _userSession.value?.uid ?: "")

        val taskArray = JSONArray()
        _tasks.value.forEach { t ->
            val obj = JSONObject()
            obj.put("title", t.title)
            obj.put("description", t.description)
            obj.put("priority", t.priority.name)
            obj.put("category", t.category.name)
            obj.put("isCompleted", t.isCompleted)
            taskArray.put(obj)
        }
        root.put("tasks", taskArray)

        val habitArray = JSONArray()
        _habits.value.forEach { h ->
            val obj = JSONObject()
            obj.put("title", h.title)
            obj.put("currentStreak", h.currentStreak)
            obj.put("bestStreak", h.bestStreak)
            habitArray.put(obj)
        }
        root.put("habits", habitArray)

        val noteArray = JSONArray()
        _notes.value.forEach { n ->
            val obj = JSONObject()
            obj.put("title", n.title)
            obj.put("content", n.content)
            noteArray.put(obj)
        }
        root.put("notes", noteArray)

        return root.toString(2)
    }

    fun deleteAccountAndData(onCompleted: () -> Unit) {
        val uid = _userSession.value?.uid ?: return
        viewModelScope.launch {
            val db = TimeflowDatabase.getInstance(context)
            db.taskDao().deleteAllForUser(uid)
            db.calendarDao().deleteAllForUser(uid)
            db.focusDao().deleteAllForUser(uid)
            db.habitDao().deleteAllForUser(uid)
            db.goalDao().deleteAllForUser(uid)
            db.studyDao().deleteAllSubjectsForUser(uid)
            db.studyDao().deleteAllTopicsForUser(uid)
            db.noteDao().deleteAllForUser(uid)
            _tasks.value = emptyList()
            _events.value = emptyList()
            _habits.value = emptyList()
            _goals.value = emptyList()
            _subjects.value = emptyList()
            _notes.value = emptyList()
            onCompleted()
        }
    }
}
