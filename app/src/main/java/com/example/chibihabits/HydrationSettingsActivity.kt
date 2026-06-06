// HydrationSettingsActivity.kt  — fixed wiring + runtime notification permission
package com.example.chibihabits

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.SimpleDateFormat
import java.util.*

class HydrationSettingsActivity : AppCompatActivity() {

    private val PREFS = "chibi_prefs"

    private lateinit var etGoal: EditText
    private lateinit var spHours: Spinner
    private lateinit var swEnable: SwitchMaterial
    private lateinit var chart: LineChart

    private lateinit var tvTodayProgress: TextView
    private lateinit var tvTodayPercent: TextView
    private lateinit var progressToday: ProgressBar
    private lateinit var btnResetToday: Button
    private lateinit var btnSave: Button

    private val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())

    private fun todayKey(base: String) =
        "${base}_${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}"

    // Android 13+ notification permission launcher
    private val askNotifPerm = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "Notification permission denied — reminders may be hidden.", Toast.LENGTH_LONG).show()
        } else {
            // If user just enabled permission and reminders are ON, keep schedule intact
            val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (sp.getBoolean("water_reminder_enabled", false)) {
                HydrationReminderScheduler.schedule(this, sp.getInt("water_reminder_hours", 2))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hydration_settings)

        // Bind views
        etGoal = findViewById(R.id.etGoalMl)
        spHours = findViewById(R.id.spReminderHours)
        swEnable = findViewById(R.id.swEnableReminders)
        chart = findViewById(R.id.lineChartHydro)

        tvTodayProgress = findViewById(R.id.tvTodayProgress)
        tvTodayPercent  = findViewById(R.id.tvTodayPercent)
        progressToday   = findViewById(R.id.progressToday)
        btnResetToday   = findViewById(R.id.btnResetToday)
        btnSave         = findViewById(R.id.btnSaveHydro)

        // Notifications channel (idempotent)
        NotificationUtils.ensureHydrationChannel(this)

        // Spinner data (1..6 hrs)
        val intervals = listOf(1, 2, 3, 4, 5, 6)
        spHours.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, intervals)

        // Load & render
        loadPrefs()
        refreshTodayCard()
        renderChart()

        // Actions
        btnSave.setOnClickListener {
            savePrefsAndSchedule()
            refreshTodayCard()
            renderChart()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }

        btnResetToday.setOnClickListener {
            val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            sp.edit()
                .putInt(todayKey("water_taken_ml"), 0)
                .remove(todayKey("water_last_added_ml"))
                .apply()
            refreshTodayCard()
            renderChart()
            Toast.makeText(this, "Today’s water log reset.", Toast.LENGTH_SHORT).show()
        }

        // (Optional UX) toggle dependent inputs when switch changes
        swEnable.setOnCheckedChangeListener { _, isChecked ->
            // No immediate scheduling here; we still persist on Save
            spHours.isEnabled = isChecked
        }
    }

    override fun onResume() {
        super.onResume()
        // In case user added water from Dashboard while this was in background
        refreshTodayCard()
        renderChart()
    }

    private fun loadPrefs() {
        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val goal = sp.getInt("water_goal_ml", 2000)
        val hours = sp.getInt("water_reminder_hours", 2)
        val enabled = sp.getBoolean("water_reminder_enabled", false)

        etGoal.setText(goal.toString())
        @Suppress("UNCHECKED_CAST")
        val adapter = spHours.adapter as? ArrayAdapter<Int>
        val idx = adapter?.getPosition(hours) ?: -1
        spHours.setSelection(if (idx >= 0) idx else 1) // default to "2"
        swEnable.isChecked = enabled
        spHours.isEnabled = enabled
    }

    private fun savePrefsAndSchedule() {
        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val goal = etGoal.text?.toString()?.toIntOrNull()?.coerceAtLeast(250) ?: 2000
        val hours = spHours.selectedItem as Int
        val enabled = swEnable.isChecked

        sp.edit()
            .putInt("water_goal_ml", goal)
            .putInt("water_reminder_hours", hours)
            .putBoolean("water_reminder_enabled", enabled)
            .apply()

        if (enabled) {
            // Android 13+ needs runtime permission for notifications
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                askNotifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            HydrationReminderScheduler.schedule(this, hours)
            val nextAt = System.currentTimeMillis() + hours * 60L * 60L * 1000L
            sp.edit().putLong("water_next_reminder_ts", nextAt).apply()
        } else {
            HydrationReminderScheduler.cancel(this)
            sp.edit().remove("water_next_reminder_ts").apply()
        }
    }

    private fun refreshTodayCard() {
        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val goal = sp.getInt("water_goal_ml", 2000).coerceAtLeast(250)
        val taken = sp.getInt(todayKey("water_taken_ml"), 0)
        val pct = if (goal > 0) ((taken * 100f) / goal).toInt().coerceIn(0, 100) else 0

        tvTodayProgress.text = "$taken / $goal ml"
        tvTodayPercent.text  = "$pct%"
        progressToday.progress = pct
    }

    private fun renderChart() {
        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val goal = sp.getInt("water_goal_ml", 2000).coerceAtLeast(250)

        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val key = "water_taken_ml_${sdfKey.format(cal.time)}"
            val taken = sp.getInt(key, 0)
            val pct = if (goal > 0) ((taken * 100f) / goal).coerceIn(0f, 100f) else 0f

            entries.add(Entry((6 - i).toFloat(), pct))
            labels.add(sdfDay.format(cal.time).uppercase(Locale.getDefault()))
        }

        val lineColor = ContextCompat.getColor(this, R.color.chibi_water)
        val set = LineDataSet(entries, "").apply {
            setDrawValues(false)
            setDrawCircles(true)
            circleRadius = 3.5f
            lineWidth = 2.5f
            color = lineColor
            setCircleColor(lineColor)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        chart.apply {
            data = LineData(set)
            axisRight.isEnabled = false
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 100f
                granularity = 25f
                setDrawGridLines(true)
            }
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
                setDrawGridLines(false)
            }
            legend.isEnabled = false
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            setBackgroundColor(ContextCompat.getColor(context, R.color.chibi_bg))
            invalidate()
        }
    }
}