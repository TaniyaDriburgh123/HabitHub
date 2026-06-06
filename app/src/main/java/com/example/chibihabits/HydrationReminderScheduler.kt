package com.example.chibihabits

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object HydrationReminderScheduler {
    private const val UNIQUE_NAME = "hydration_reminder_periodic"
    private const val TAG = "HydrationScheduler"

    fun schedule(ctx: Context, hours: Int) {
        val h = hours.coerceIn(1, 6)

        Log.d(TAG, "Scheduling hydration reminders every $h hours")

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .setRequiresStorageNotLow(false)
            .build()

        val req = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            h.toLong(),
            TimeUnit.HOURS,
            15, // flex interval - 15 minutes
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("hydration")
            .build()

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            req
        )

        // Set initial next reminder time
        val sp = ctx.getSharedPreferences("chibi_prefs", Context.MODE_PRIVATE)
        val nextAt = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(h.toLong())
        sp.edit().putLong("water_next_reminder_ts", nextAt).apply()
    }

    fun cancel(ctx: Context) {
        Log.d(TAG, "Canceling hydration reminders")
        WorkManager.getInstance(ctx).cancelUniqueWork(UNIQUE_NAME)

        val sp = ctx.getSharedPreferences("chibi_prefs", Context.MODE_PRIVATE)
        sp.edit().remove("water_next_reminder_ts").apply()
    }

    fun isScheduled(ctx: Context): Boolean {
        return try {
            val workManager = WorkManager.getInstance(ctx)
            val workInfo = workManager.getWorkInfosForUniqueWork(UNIQUE_NAME).get()
            workInfo.any { it.state == WorkInfo.State.ENQUEUED }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking schedule status", e)
            false
        }
    }
}