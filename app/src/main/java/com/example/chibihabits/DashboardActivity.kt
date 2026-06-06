package com.example.chibihabits

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {
    private val PREFS = "chibi_prefs"

    private fun todayKey(base: String): String {
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return "${base}_$d"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        refreshGreeting() // <-- NEW: set greeting immediately

        // ==== Habits ====
        findViewById<Button>(R.id.btnOpenHabits)?.setOnClickListener {
            startActivity(Intent(this, HabitsActivity::class.java))
        }

        // ==== Mood quick log (8 moods) ====
        findViewById<Button>(R.id.btnMood1)?.setOnClickListener { logMood("😊") }
        findViewById<Button>(R.id.btnMood2)?.setOnClickListener { logMood("😌") }
        findViewById<Button>(R.id.btnMood3)?.setOnClickListener { logMood("😕") }
        findViewById<Button>(R.id.btnMood4)?.setOnClickListener { logMood("😢") }
        findViewById<Button>(R.id.btnMood5)?.setOnClickListener { logMood("😡") }
        findViewById<Button>(R.id.btnMood6)?.setOnClickListener { logMood("🤩") }
        findViewById<Button>(R.id.btnMood7)?.setOnClickListener { logMood("😴") }
        findViewById<Button>(R.id.btnMood8)?.setOnClickListener { logMood("🤒") }

        findViewById<Button>(R.id.btnOpenMood)?.setOnClickListener {
            startActivity(Intent(this, MoodJournalActivity::class.java))
        }
        findViewById<Button>(R.id.btnOpenMoodChart)?.setOnClickListener {
            startActivity(Intent(this, MoodChartActivity::class.java))
        }

        // ==== Hydration widget ====
        fun addWater(amount: Int) {
            val key = todayKey("water_taken_ml")
            val spLocal = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val taken = spLocal.getInt(key, 0) + amount
            spLocal.edit()
                .putInt(key, taken)
                .putInt(todayKey("water_last_added_ml"), amount)
                .apply()
            refreshHydrationCard()
        }

        findViewById<MaterialButton>(R.id.btnAddSmall)?.setOnClickListener { addWater(150) }
        findViewById<MaterialButton>(R.id.btnAddMedium)?.setOnClickListener { addWater(250) }
        findViewById<MaterialButton>(R.id.btnAddLarge)?.setOnClickListener { addWater(500) }

        findViewById<MaterialButton>(R.id.btnOpenHydration)?.setOnClickListener {
            startActivity(Intent(this, HydrationSettingsActivity::class.java))
        }

        // ==== Profile ====
        findViewById<Button>(R.id.btnOpenProfile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        refreshAll()
    }

    override fun onResume() {
        super.onResume()
        refreshAll()
        refreshGreeting() // <-- NEW: update greeting after returning from Profile
    }

    private fun refreshAll() {
        refreshHabitsCard()
        refreshMoodCard()
        refreshHydrationCard()
    }

    // NEW: reads name from SharedPreferences
    private fun refreshGreeting() {
        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val name = sp.getString("name", "Chibi")
        findViewById<TextView>(R.id.tvGreeting)?.text = "Hi, $name 👋"
    }

    // ==== Habits ====
    private fun refreshHabitsCard() {
        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val total = sp.getInt(todayKey("habits_total"), 0)
        val done = sp.getInt(todayKey("habits_done"), 0)
        val percent = if (total > 0) (done * 100 / total) else 0
        findViewById<TextView>(R.id.tvHabitPercent)?.text = "$percent%"
        findViewById<ProgressBar>(R.id.progressHabits)?.progress = percent
    }

    // ==== Mood ====
    private fun logMood(emoji: String) {
        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val sdfDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("h:mm a", Locale.getDefault())
        val sdfStamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val dayKey = sdfDay.format(Date())
        val timeNow = sdfTime.format(Date())
        val stamp = sdfStamp.format(Date())

        val score = when (emoji) {
            "😊", "🤩" -> 4
            "😌"      -> 3
            "😕", "😴" -> 2
            "😢", "😡", "🤒" -> 1
            else -> 0
        }

        val listItem = "$stamp | $timeNow — $emoji"
        val keyHistory = "mood_history_$dayKey"
        val old = sp.getStringSet(keyHistory, emptySet()) ?: emptySet()
        val newSet = HashSet(old).apply { add(listItem) }

        sp.edit()
            .putString("mood_last_emoji", emoji)
            .putString("mood_last_time", timeNow)
            .putString("mood_last_emoji_$dayKey", emoji)
            .putString("mood_last_time_$dayKey", timeNow)
            .putInt("mood_score_$dayKey", score)
            .putStringSet(keyHistory, newSet)
            .apply()

        refreshMoodCard()
    }

    private fun refreshMoodCard() {
        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lastMood = sp.getString("mood_last_emoji", "—")
        val lastTime = sp.getString("mood_last_time", "")
        findViewById<TextView>(R.id.tvLastMood)?.text =
            if (lastTime.isNullOrBlank()) "Last: $lastMood" else "Last: $lastMood • $lastTime"
    }

    // ==== Hydration ====
    private fun refreshHydrationCard() {
        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val goal = sp.getInt("water_goal_ml", 2000)
        val taken = sp.getInt(todayKey("water_taken_ml"), 0)
        val pct = if (goal > 0) (taken * 100 / goal).coerceIn(0, 100) else 0

        findViewById<TextView>(R.id.tvWaterInfo)?.text = "$taken / $goal ml"
        findViewById<TextView>(R.id.tvWaterPercent)?.text = "$pct%"
        findViewById<ProgressBar>(R.id.progressWater)?.progress = pct

        val enabled = sp.getBoolean("water_reminder_enabled", false)
        val nextTs = sp.getLong("water_next_reminder_ts", 0L)
        val status = if (!enabled) {
            "Reminders: Off"
        } else {
            if (nextTs > System.currentTimeMillis()) {
                val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
                "Reminders: On • Next at ${fmt.format(Date(nextTs))}"
            } else {
                "Reminders: On"
            }
        }
        findViewById<TextView>(R.id.tvReminderStatus)?.text = status
    }
}
