package com.example

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.notification.NotificationScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class Achievement(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = HabitRepository(db.habitDao(), db.journalDao())
    private val prefs = PreferenceRepository(application)

    // Achievements master definition
    val achievementsList = listOf(
        Achievement("streak_3", "Primeros pasos", "🔥", "3 días seguidos con racha"),
        Achievement("streak_7", "En racha", "🔥", "7 días seguidos con racha"),
        Achievement("streak_14", "Imparable", "💥", "14 días seguidos con racha"),
        Achievement("streak_30", "Leyenda", "⚡", "30 días seguidos con racha"),
        Achievement("streak_60", "Mítico", "🌟", "60 días seguidos con racha"),
        Achievement("streak_100", "Dios del hábito", "👑", "100 días seguidos con racha"),
        Achievement("level_5", "Aprendiz", "📖", "Alcanzar nivel 5"),
        Achievement("level_10", "Disciplinado", "🎯", "Alcanzar nivel 10"),
        Achievement("level_25", "Maestro", "🏅", "Alcanzar nivel 25"),
        Achievement("level_50", "Gran Maestro", "💎", "Alcanzar nivel 50"),
        Achievement("total_xp_500", "Medio kilo", "💰", "Acumular 500 XP total"),
        Achievement("total_xp_2000", "Dos kilos", "💰", "Acumular 2000 XP total"),
        Achievement("total_xp_5000", "Cinco kilos", "💰", "Acumular 5000 XP total"),
        Achievement("total_xp_10000", "Diez kilos", "💰", "Acumular 10000 XP total"),
        Achievement("perfect_week", "Semana perfecta", "🏆", "Completar 100% de una semana"),
        Achievement("all_habits_day", "Día perfecto", "⭐", "Completar todos los hábitos un día"),
        Achievement("journal_7", "Escritor", "✍️", "Escribir 7 entradas en el diario"),
        Achievement("journal_30", "Cronista", "📝", "Escribir 30 entradas en el diario"),
        Achievement("avoid_7", "Resistencia", "🛡️", "7 días evitando un mal hábito"),
        Achievement("avoid_30", "Inquebrantable", "🚫", "30 días evitando un mal hábito")
    )

    // Flows from database
    val habits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journalEntries: StateFlow<List<JournalEntity>> = repository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings & Level state
    private val _totalXp = MutableStateFlow(prefs.totalXp)
    val totalXp: StateFlow<Int> = _totalXp.asStateFlow()

    private val _dailyGoal = MutableStateFlow(prefs.dailyGoal)
    val dailyGoal: StateFlow<Int> = _dailyGoal.asStateFlow()

    private val _autoGoal = MutableStateFlow(prefs.autoGoal)
    val autoGoal: StateFlow<Boolean> = _autoGoal.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.notificationsEnabled)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _notificationInterval = MutableStateFlow(prefs.notificationInterval)
    val notificationInterval: StateFlow<Int> = _notificationInterval.asStateFlow()

    private val _smartNotifications = MutableStateFlow(prefs.smartNotifications)
    val smartNotifications: StateFlow<Boolean> = _smartNotifications.asStateFlow()

    private val _unlockedAchievements = MutableStateFlow(prefs.unlockedAchievements)
    val unlockedAchievements: StateFlow<Set<String>> = _unlockedAchievements.asStateFlow()

    // Triggered event for showing newly unlocked achievement modals
    private val _newlyUnlockedAchievement = MutableStateFlow<Achievement?>(null)
    val newlyUnlockedAchievement: StateFlow<Achievement?> = _newlyUnlockedAchievement.asStateFlow()

    init {
        // Schedule notification on initialize
        syncNotifications()
    }

    // Helper: current date formatted YYYY-MM-DD
    fun getLocalDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Level computations: 100 XP per level
    val currentLevel: StateFlow<Int> = totalXp
        .map { xp -> (xp / 100) + 1 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1)

    val nextLevelProgress: StateFlow<Float> = totalXp
        .map { xp -> (xp % 100) / 100f }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    // CRUD Habits
    fun addHabit(
        name: String,
        emoji: String,
        habitType: String,
        xpReward: Int,
        progressive: Boolean,
        unit: String?,
        minAmount: Int?,
        barrierBonus: Int?,
        frequency: String
    ) {
        viewModelScope.launch {
            val newHabit = HabitEntity(
                id = "h_" + System.currentTimeMillis(),
                name = name,
                emoji = emoji,
                habitType = habitType,
                xpReward = xpReward,
                progressive = progressive,
                unit = unit,
                minAmount = minAmount,
                barrierBonus = barrierBonus,
                frequency = frequency
            )
            repository.insertHabit(newHabit)
            checkAchievements()
        }
    }

    fun deleteHabit(id: String) {
        viewModelScope.launch {
            repository.deleteHabitById(id)
        }
    }

    fun archiveHabit(id: String, archived: Boolean) {
        viewModelScope.launch {
            repository.getHabitById(id)?.let { habit ->
                repository.insertHabit(habit.copy(archived = archived))
            }
        }
    }

    // Habit Interactivity: Toggle completions (standard habits)
    fun toggleHabitCompletion(id: String) {
        viewModelScope.launch {
            val today = getLocalDate()
            repository.getHabitById(id)?.let { habit ->
                val currentCompletions = habit.completions.toMutableList()
                val xpIncrement: Int

                if (currentCompletions.contains(today)) {
                    currentCompletions.remove(today)
                    // If build, remove positive XP. If avoid, they had failed today and now we undo failure, so add back positive XP.
                    xpIncrement = if (habit.habitType == "build") -habit.xpReward else habit.xpReward
                } else {
                    currentCompletions.add(today)
                    // If build, add positive XP. If avoid, they fall today, so deduct negative XP!
                    xpIncrement = if (habit.habitType == "build") habit.xpReward else -habit.xpReward
                }

                val updated = habit.copy(completions = currentCompletions)
                repository.insertHabit(updated)
                adjustXp(xpIncrement)
                checkAchievements()
            }
        }
    }

    // Habit Interactivity: Skip habit today
    fun toggleHabitSkip(id: String) {
        viewModelScope.launch {
            val today = getLocalDate()
            repository.getHabitById(id)?.let { habit ->
                val currentSkips = habit.skips.toMutableList()
                if (currentSkips.contains(today)) {
                    currentSkips.remove(today)
                } else {
                    currentSkips.add(today)
                    // Remove completion if it exists
                    val currentCompletions = habit.completions.toMutableList()
                    if (currentCompletions.contains(today)) {
                        currentCompletions.remove(today)
                        val xpRestore = if (habit.habitType == "build") -habit.xpReward else habit.xpReward
                        adjustXp(xpRestore)
                    }
                }
                val updated = habit.copy(skips = currentSkips)
                repository.insertHabit(updated)
                checkAchievements()
            }
        }
    }

    // Habit Interactivity: Update progressive numeric amount
    fun updateProgressiveAmount(id: String, newAmount: Int) {
        viewModelScope.launch {
            val today = getLocalDate()
            repository.getHabitById(id)?.let { habit ->
                val currentAmounts = habit.amounts.toMutableMap()
                val oldAmount = currentAmounts[today] ?: 0
                
                currentAmounts[today] = newAmount
                
                // Calculate incremental XP difference
                val oldXp = getHabitProgressiveXp(habit, oldAmount)
                val newXp = getHabitProgressiveXp(habit, newAmount)
                val diffXp = newXp - oldXp

                // Update database
                val updated = habit.copy(amounts = currentAmounts)
                repository.insertHabit(updated)
                adjustXp(diffXp)
                checkAchievements()
            }
        }
    }

    // Journal Entry CRUD
    fun addJournalEntry(mood: Int, text: String) {
        viewModelScope.launch {
            val xpGain = 15 // Standard gamified diary incentive
            val newEntry = JournalEntity(
                date = getLocalDate(),
                mood = mood,
                text = text,
                xp = xpGain
            )
            repository.insertJournalEntry(newEntry)
            adjustXp(xpGain)
            checkAchievements()
        }
    }

    fun deleteJournalEntry(entry: JournalEntity) {
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
            adjustXp(-entry.xp)
        }
    }

    // Settings updaters
    fun setDailyGoal(goal: Int) {
        prefs.dailyGoal = goal
        _dailyGoal.value = goal
    }

    fun setAutoGoal(auto: Boolean) {
        prefs.autoGoal = auto
        _autoGoal.value = auto
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.notificationsEnabled = enabled
        _notificationsEnabled.value = enabled
        syncNotifications()
    }

    fun setNotificationInterval(intervalHours: Int) {
        prefs.notificationInterval = intervalHours
        _notificationInterval.value = intervalHours
        syncNotifications()
    }

    fun setSmartNotifications(smart: Boolean) {
        prefs.smartNotifications = smart
        _smartNotifications.value = smart
    }

    fun dismissAchievementDialog() {
        _newlyUnlockedAchievement.value = null
    }

    fun resetAllData() {
        viewModelScope.launch {
            // Delete habits and journals
            val activeHabits = habits.value
            activeHabits.forEach { repository.deleteHabitById(it.id) }
            
            val activeJournals = journalEntries.value
            activeJournals.forEach { repository.deleteJournalEntry(it) }

            prefs.totalXp = 0
            prefs.dailyGoal = 50
            prefs.autoGoal = true
            prefs.notificationsEnabled = true
            prefs.notificationInterval = 12
            prefs.smartNotifications = true
            prefs.clearAchievements()

            _totalXp.value = 0
            _dailyGoal.value = 50
            _autoGoal.value = true
            _notificationsEnabled.value = true
            _notificationInterval.value = 12
            _smartNotifications.value = true
            _unlockedAchievements.value = emptySet()
            _newlyUnlockedAchievement.value = null

            syncNotifications()
        }
    }

    // Instant Notification Live Testing
    fun triggerTestNotification() {
        NotificationScheduler.triggerImmediate(getApplication())
    }

    private fun syncNotifications() {
        if (prefs.notificationsEnabled) {
            NotificationScheduler.scheduleAlarms(getApplication(), prefs.notificationInterval)
        } else {
            NotificationScheduler.cancelAlarms(getApplication())
        }
    }

    private fun adjustXp(increment: Int) {
        val nextXp = (prefs.totalXp + increment).coerceAtLeast(0)
        prefs.totalXp = nextXp
        _totalXp.value = nextXp
    }

    // Gamification Engine algorithms
    fun getHabitStreak(habit: HabitEntity): Int {
        val completions = habit.completions.toSet()
        val skips = habit.skips.toSet()
        if (completions.isEmpty()) return 0

        var streak = 0
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val todayStr = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        // Reset calendar
        cal.time = Date()

        // If neither today nor yesterday is completed/skipped, streak is broken
        val hasActivityRecently = completions.contains(todayStr) || skips.contains(todayStr) ||
                completions.contains(yesterdayStr) || skips.contains(yesterdayStr)
        if (!hasActivityRecently) return 0

        // If today is completed or skipped, start checking from today; otherwise start from yesterday
        val startWithToday = completions.contains(todayStr) || skips.contains(todayStr)
        if (!startWithToday) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val dateStr = sdf.format(cal.time)
            if (completions.contains(dateStr)) {
                streak++
            } else if (skips.contains(dateStr)) {
                // Skips maintain the streak but do not increment it
            } else {
                break
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
            // Limit loop sanity check
            if (streak > 500) break
        }
        return streak
    }

    fun getHabitRate(habit: HabitEntity): Float {
        // Return completion rate for the last 7 days
        val completions = habit.completions.toSet()
        val skips = habit.skips.toSet()
        var completedCount = 0
        var totalTrackedDays = 7

        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 0 until 7) {
            val dateStr = sdf.format(cal.time)
            if (completions.contains(dateStr)) {
                completedCount++
            } else if (skips.contains(dateStr)) {
                totalTrackedDays-- // skipped doesn't count against rate
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        if (totalTrackedDays <= 0) return 0f
        return completedCount.toFloat() / totalTrackedDays
    }

    // Get exact XP generated by a habit for a given day
    fun getHabitDayXp(habit: HabitEntity, date: String): Int {
        val completed = habit.completions.contains(date)
        val skipped = habit.skips.contains(date)
        if (skipped) return 0

        return if (habit.progressive) {
            val amount = habit.amounts[date] ?: 0
            getHabitProgressiveXp(habit, amount)
        } else {
            if (habit.habitType == "build") {
                if (completed) habit.xpReward else 0
            } else { // avoid
                // Avoid habit pays XP automatically if you didn't mark as "Fell" (completions represents failure/falling in avoid habits)
                if (completed) -habit.xpReward else habit.xpReward
            }
        }
    }

    // Helper: Progressive XP formula matching Chat Z's logic
    private fun getHabitProgressiveXp(habit: HabitEntity, amount: Int): Int {
        val min = habit.minAmount ?: 1
        val barrier = habit.barrierBonus ?: (min * 2)
        if (amount <= 0) return 0
        return if (amount >= min) {
            barrier + (amount - min) // Barrier met + 1 XP per extra unit
        } else {
            0 // arrancar es lo más difícil, no met barrier = 0 XP
        }
    }

    // Evaluate Achievements state machine
    private fun checkAchievements() {
        val xp = totalXp.value
        val lvl = currentLevel.value
        val hList = habits.value.filter { !it.archived }
        val journalsCount = journalEntries.value.size

        // Calculate streaks
        val maxStreak = hList.maxOfOrNull { getHabitStreak(it) } ?: 0
        val maxAvoidStreak = hList.filter { it.habitType == "avoid" }.maxOfOrNull { getHabitStreak(it) } ?: 0

        // Check each def
        achievementsList.forEach { ach ->
            var unlocks = false
            when (ach.id) {
                "streak_3" -> unlocks = maxStreak >= 3
                "streak_7" -> unlocks = maxStreak >= 7
                "streak_14" -> unlocks = maxStreak >= 14
                "streak_30" -> unlocks = maxStreak >= 30
                "streak_60" -> unlocks = maxStreak >= 60
                "streak_100" -> unlocks = maxStreak >= 100
                "level_5" -> unlocks = lvl >= 5
                "level_10" -> unlocks = lvl >= 10
                "level_25" -> unlocks = lvl >= 25
                "level_50" -> unlocks = lvl >= 50
                "total_xp_500" -> unlocks = xp >= 500
                "total_xp_2000" -> unlocks = xp >= 2000
                "total_xp_5000" -> unlocks = xp >= 5000
                "total_xp_10000" -> unlocks = xp >= 10000
                "perfect_week" -> unlocks = maxStreak >= 7
                "all_habits_day" -> {
                    // Quick check: all daily build habits are completed today
                    val today = getLocalDate()
                    val activeDailyBuilds = hList.filter { it.habitType == "build" && it.frequency == "daily" }
                    unlocks = activeDailyBuilds.isNotEmpty() && activeDailyBuilds.all { it.completions.contains(today) || it.skips.contains(today) }
                }
                "journal_7" -> unlocks = journalsCount >= 7
                "journal_30" -> unlocks = journalsCount >= 30
                "avoid_7" -> unlocks = maxAvoidStreak >= 7
                "avoid_30" -> unlocks = maxAvoidStreak >= 30
            }

            if (unlocks) {
                val newlyUnlocked = prefs.unlockAchievement(ach.id)
                if (newlyUnlocked) {
                    _unlockedAchievements.value = prefs.unlockedAchievements
                    _newlyUnlockedAchievement.value = ach
                }
            }
        }
    }
}
