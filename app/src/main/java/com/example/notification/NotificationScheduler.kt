package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log

object NotificationScheduler {
    fun scheduleAlarms(context: Context, intervalHours: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HabitNotificationReceiver::class.java)
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, pendingIntentFlags)

        // Convert hours to milliseconds
        val intervalMs = intervalHours * 60 * 60 * 1000L
        val triggerAtMs = SystemClock.elapsedRealtime() + intervalMs

        alarmManager.cancel(pendingIntent) // Cancel previous alarms
        
        try {
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerAtMs,
                intervalMs,
                pendingIntent
            )
            Log.d("NotificationScheduler", "Scheduled repeating alarm every $intervalHours hours.")
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Failed to schedule alarm", e)
        }
    }

    fun cancelAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HabitNotificationReceiver::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, pendingIntentFlags)
        alarmManager.cancel(pendingIntent)
    }
    
    // Instantly fires the reminder receiver (excellent for live-testing notifications in-app)
    fun triggerImmediate(context: Context) {
        val intent = Intent(context, HabitNotificationReceiver::class.java)
        context.sendBroadcast(intent)
    }
}
