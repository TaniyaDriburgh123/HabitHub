package com.example.chibihabits

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class HydrationReminderWorker(
    private val ctx: Context,
    params: WorkerParameters
) : Worker(ctx, params) {

    override fun doWork(): Result {
        val sp = ctx.getSharedPreferences("chibi_prefs", Context.MODE_PRIVATE)
        val enabled = sp.getBoolean("water_reminder_enabled", false)
        if (!enabled) return Result.success()

        NotificationUtils.ensureHydrationChannel(ctx)
        val notif = NotificationCompat.Builder(ctx, NotificationUtils.HYDRATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water) // see vector below
            .setContentTitle("Hydration Reminder")
            .setContentText("Time to drink some water 💧")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notif)

        val hours = sp.getInt("water_reminder_hours", 2).coerceIn(1, 6)
        val nextAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hours.toLong())
        sp.edit().putLong("water_next_reminder_ts", nextAt).apply()

        return Result.success()
    }
}
