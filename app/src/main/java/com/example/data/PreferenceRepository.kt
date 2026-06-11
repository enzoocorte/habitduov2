package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("habit_duo_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOTAL_XP = "total_xp"
        private const val KEY_DAILY_GOAL = "daily_goal"
        private const val KEY_AUTO_GOAL = "auto_goal"
        private const val KEY_NOTIFICATIONS = "notifications"
        private const val KEY_NOTIFICATION_INTERVAL = "notification_interval"
        private const val KEY_SMART_NOTIFICATIONS = "smart_notifications"
        private const val KEY_UNLOCKED_ACHIEVEMENTS = "unlocked_achievements"
    }

    var totalXp: Int
        get() = prefs.getInt(KEY_TOTAL_XP, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_XP, value).apply()

    var dailyGoal: Int
        get() = prefs.getInt(KEY_DAILY_GOAL, 50)
        set(value) = prefs.edit().putInt(KEY_DAILY_GOAL, value).apply()

    var autoGoal: Boolean
        get() = prefs.getBoolean(KEY_AUTO_GOAL, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_GOAL, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    var notificationInterval: Int // custom hour interval, default 12 hours
        get() = prefs.getInt(KEY_NOTIFICATION_INTERVAL, 12)
        set(value) = prefs.edit().putInt(KEY_NOTIFICATION_INTERVAL, value).apply()

    var smartNotifications: Boolean
        get() = prefs.getBoolean(KEY_SMART_NOTIFICATIONS, true)
        set(value) = prefs.edit().putBoolean(KEY_SMART_NOTIFICATIONS, value).apply()

    var unlockedAchievements: Set<String>
        get() = prefs.getStringSet(KEY_UNLOCKED_ACHIEVEMENTS, emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_UNLOCKED_ACHIEVEMENTS, value).apply()

    fun unlockAchievement(id: String): Boolean {
        val current = unlockedAchievements.toMutableSet()
        if (current.add(id)) {
            unlockedAchievements = current
            return true // newly unlocked
        }
        return false
    }

    fun clearAchievements() {
        prefs.edit().remove(KEY_UNLOCKED_ACHIEVEMENTS).apply()
    }
}
