package com.example.chibihabits

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.chibihabits.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

class MoodChartActivity : AppCompatActivity() {

    private val PREFS = "chibi_prefs"

    private lateinit var chart: LineChart
    private lateinit var tvEmpty: TextView
    private lateinit var btn7: MaterialButton
    private lateinit var btn14: MaterialButton
    private lateinit var btn30: MaterialButton

    private val sdfStore = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())

    // emoji → score (1..4)
    private val scoreOf = mapOf(
        "😊" to 4, "🤩" to 4,
        "😌" to 3,
        "😕" to 2, "😴" to 2,
        "😢" to 1, "😡" to 1, "🤒" to 1
    )

    // Y-axis labels for 0..4
    private val yLabels = arrayOf("—", "😢/😡/🤒", "😕/😴", "😌", "😊/🤩")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_chart)

        chart = findViewById(R.id.lineChart)
        tvEmpty = findViewById(R.id.tvEmpty)
        btn7 = findViewById(R.id.btn7)
        btn14 = findViewById(R.id.btn14)
        btn30 = findViewById(R.id.btn30)

        // initial render = 7 days
        setSelectedRangeButton(btn7)
        render(7)

        btn7.setOnClickListener { setSelectedRangeButton(btn7); render(7) }
        btn14.setOnClickListener { setSelectedRangeButton(btn14); render(14) }
        btn30.setOnClickListener { setSelectedRangeButton(btn30); render(30) }
    }

    private fun render(days: Int) {
        val sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        // Build left→right across the chosen range
        for (i in days - 1 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateKey = sdfStore.format(cal.time)
            val label = sdfDay.format(cal.time).uppercase(Locale.getDefault())

            val avg = averageScoreForDay(sp, dateKey)
            entries.add(Entry((days - 1 - i).toFloat(), avg.toFloat()))
            labels.add(label)
        }

        val hasAny = entries.any { it.y > 0f }
        tvEmpty.visibility = if (hasAny) View.GONE else View.VISIBLE
        chart.visibility = if (hasAny) View.VISIBLE else View.INVISIBLE
        if (!hasAny) return

        val colorLine = ContextCompat.getColor(this, R.color.chibi_light_green)
        val colorDot = ContextCompat.getColor(this, R.color.chibi_green)

        val dataSet = LineDataSet(entries, "").apply {
            setDrawValues(false)
            setDrawCircles(true)
            circleRadius = 4f
            lineWidth = 2.5f
            color = colorLine
            setCircleColor(colorDot)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        chart.apply {
            data = LineData(dataSet)

            axisRight.isEnabled = false
            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 4f
                granularity = 1f
                setDrawGridLines(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val idx = value.roundToInt().coerceIn(0, 4)
                        return yLabels[idx]
                    }
                }
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(labels)
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

    /**
     * Average mood score for a given yyyy-MM-dd.
     * Prefers full per-day history (mood_history_YYYY-MM-DD),
     * falls back to legacy mood_score_YYYY-MM-DD.
     */
    private fun averageScoreForDay(sp: android.content.SharedPreferences, dateKey: String): Int {
        val set = sp.getStringSet("mood_history_$dateKey", null)
        if (!set.isNullOrEmpty()) {
            // entries like: "HH:mm:ss | h:mm a — 😊"
            val scores = set.mapNotNull { it.substringAfter("— ").takeIf { e -> e.isNotBlank() } }
                .mapNotNull { scoreOf[it] }
            if (scores.isNotEmpty()) {
                return (scores.sum().toFloat() / scores.size).roundToInt()
            }
        }
        return sp.getInt("mood_score_$dateKey", 0)
    }

    /**
     * Simple visual selection for the 7/14/30d buttons
     */
    private fun setSelectedRangeButton(selected: MaterialButton) {
        val all = listOf(btn7, btn14, btn30)
        val active = ContextCompat.getColor(this, R.color.chibi_green)
        val inactive = ContextCompat.getColor(this, R.color.chibi_light_green)
        val white = ContextCompat.getColor(this, android.R.color.white)

        all.forEach { b ->
            val isSelected = b == selected
            b.backgroundTintList = ColorStateList.valueOf(if (isSelected) active else inactive)
            b.setTextColor(white)
            b.strokeWidth = if (isSelected) 0 else 0
        }
    }
}
