package com.example.chibihabits

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MoodJournalActivity : AppCompatActivity() {
    private val PREFS = "chibi_prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_journal)

        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val calendar = findViewById<CalendarView>(R.id.calendarMood)
        val tvMoodDay = findViewById<TextView>(R.id.tvMoodDay)
        val list = findViewById<ListView>(R.id.listMoodDay)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun loadAndShow(dateKey: String) {
            val keyHistory = "mood_history_$dateKey"
            val set = sp.getStringSet(keyHistory, emptySet()) ?: emptySet()

            if (set.isEmpty()) {
                tvMoodDay.text = "No mood logged on $dateKey"
                list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listOf<String>())
                return
            }

            // sort by the "HH:mm:ss | ..." prefix descending (most recent first)
            val items = set.toList().sortedByDescending { it.substring(0, 8) }
                // show user-friendly: "h:mm a — 😊" (drop the HH:mm:ss | prefix)
                .map { it.substringAfter(" | ") }

            tvMoodDay.text = "Moods on $dateKey"
            list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        }

        // initial load for today
        val today = sdf.format(Date())
        loadAndShow(today)

        // change by calendar selection
        calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            loadAndShow(sdf.format(cal.time))
        }
    }
}
