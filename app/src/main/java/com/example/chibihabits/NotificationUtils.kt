package com.example.chibihabits

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationUtils {
    const val HYDRATION_CHANNEL_ID = "hydration_channel"
    const val HYDRATION_NOTIFICATION_ID = 1001

    fun ensureHydrationChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(
                HYDRATION_CHANNEL_ID,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you to drink water at your chosen interval"
                enableLights(true)
                lightColor = ctx.getColor(R.color.chibi_teal)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
            }
            mgr.createNotificationChannel(ch)
        }
    }

    fun createHydrationNotification(ctx: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(ctx, HYDRATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water)
            .setContentTitle("💧 Time to Hydrate!")
            .setContentText("Your body needs water to stay healthy and energized")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Stay hydrated! Drinking water regularly helps with concentration, energy levels, and overall health. Take a moment to drink a glass of water. 💦"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(ctx.getColor(R.color.chibi_teal))
    }
}