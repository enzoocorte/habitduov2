package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.PreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitNotificationReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "habit_duo_reminders"
        const val NOTIFICATION_ID = 1001
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferenceRepository(context)
        if (!prefs.notificationsEnabled) return

        // Action boot completed or general alarm
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationScheduler.scheduleAlarms(context, prefs.notificationInterval)
            return
        }

        // Fetch pending habits in a background coroutine
        val db = AppDatabase.getDatabase(context)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habits = db.habitDao().getAllHabits().first() // Read once
                val activeHabits = habits.filter { !it.archived }
                
                if (activeHabits.isEmpty()) {
                    showNotification(
                        context,
                        "🎯 ¡Empieza hoy mismo!",
                        "Crea nuevos hábitos en Habit Duo para progresar y ganar tus primeros logros."
                    )
                    return@launch
                }

                val pendingBuild = activeHabits.filter {
                    it.habitType == "build" && !it.completions.contains(today) && !it.skips.contains(today)
                }

                val title: String
                val text: String

                if (pendingBuild.isEmpty()) {
                    if (prefs.smartNotifications) {
                        title = "🏆 ¡Día impecable de hábitos!"
                        text = "Has completado todos tus hábitos para hoy. ¡Sigue así y cuida tus rachas!"
                    } else {
                        title = "🎯 Tu check-in diario en Habit Duo"
                        text = "No dejes que se te olvide escribir en tu diario y registrar tu día."
                    }
                } else {
                    if (pendingBuild.size == 1) {
                        val h = pendingBuild.first()
                        title = "${h.emoji} ¡No olvides tu racha de ${h.name}!"
                        text = "Comprométete hoy para ganar +${h.xpReward} XP y elevar tu disciplina."
                    } else {
                        title = "⚡ ¿Listos para el desafío hoy?"
                        text = "Tienes ${pendingBuild.size} hábitos de construcción pendientes hoy. ¡Ven y marca tu progreso!"
                    }
                }

                showNotification(context, title, text)
            } catch (e: Exception) {
                // Fail-safe default
                showNotification(
                    context,
                    "🎯 Mantén el hábito, cuida tu racha",
                    "Abre Habit Duo para complementar tus hábitos y registrar tu estado de ánimo diario."
                )
            }
        }
    }

    private fun showNotification(context: Context, title: String, text: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de Hábitos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones inteligentes para ayudarte a mantener tus rachas de hábitos."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, pendingIntentFlags)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // System drawable placeholder for simplicity
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
