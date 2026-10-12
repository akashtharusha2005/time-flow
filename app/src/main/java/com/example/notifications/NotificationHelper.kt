package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.R

class NotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_TASKS = "channel_timeflow_tasks"
        const val CHANNEL_FOCUS = "channel_timeflow_focus"
        const val CHANNEL_HABITS = "channel_timeflow_habits"
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val taskChannel = NotificationChannel(
                CHANNEL_TASKS,
                "Tasks & Deadlines",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled tasks and upcoming deadlines"
                enableVibration(true)
            }

            val focusChannel = NotificationChannel(
                CHANNEL_FOCUS,
                "Focus Sessions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when Pomodoro focus and break intervals finish"
                enableVibration(true)
            }

            val habitChannel = NotificationChannel(
                CHANNEL_HABITS,
                "Habits & Study",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily habit check-ins and exam countdown reminders"
            }

            manager.createNotificationChannels(listOf(taskChannel, focusChannel, habitChannel))
        }
    }

    fun showFocusFinishedNotification(title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_FOCUS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(1001, notification)
        vibrateDevice(500)
    }

    fun vibrateDevice(milliseconds: Long = 400) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(milliseconds)
            }
        } catch (_: Exception) {}
    }
}
