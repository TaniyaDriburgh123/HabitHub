package com.example.chibihabits

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class HabitsActivity : AppCompatActivity(), HabitAdapter.HabitCallbacks {

    private val PREFS = "chibi_prefs"
    private lateinit var sp: android.content.SharedPreferences

    private lateinit var rv: RecyclerView
    private lateinit var etHabit: EditText
    private lateinit var btnSave: Button
    private lateinit var adapter: HabitAdapter

    private var habits: MutableList<String> = mutableListOf()
    private var doneToday: MutableSet<String> = mutableSetOf()
    private var editingIndex: Int? = null

    private fun todayKey(base: String): String {
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return "${base}_$d"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habits)

        sp = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        rv = findViewById(R.id.rvHabits)
        etHabit = findViewById(R.id.etHabit)
        btnSave = findViewById(R.id.btnSaveHabit)

        // Load persisted data
        habits = sp.getStringSet("habits_list", emptySet())!!.toMutableList()
        doneToday = sp.getStringSet(todayKey("habits_done_set"), emptySet())!!.toMutableSet()

        // Setup RecyclerView
        adapter = HabitAdapter(habits, doneToday, this)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        // Keep dashboard progress in sync
        writeProgressInts()

        // Add / Update habit
        btnSave.setOnClickListener {
            val text = etHabit.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            if (editingIndex == null) {
                // Add
                if (habits.contains(text)) {
                    Toast.makeText(this, "Habit already exists", Toast.LENGTH_SHORT).show()
                } else {
                    habits.add(text)
                    persistHabits()
                    adapter.notifyItemInserted(habits.lastIndex)
                }
            } else {
                // Edit (rename)
                val index = editingIndex!!
                val oldName = habits[index]
                habits[index] = text
                persistHabits()

                // Migrate today's done mark if needed
                if (oldName in doneToday) {
                    doneToday.remove(oldName)
                    doneToday.add(text)
                    persistDoneToday()
                }

                adapter.notifyItemChanged(index)
                editingIndex = null
                btnSave.text = "Add"
            }

            etHabit.text.clear()
            writeProgressInts()
        }
    }

    // ==== Adapter callbacks ====
    override fun onToggle(name: String, isChecked: Boolean, position: Int) {
        if (isChecked) doneToday.add(name) else doneToday.remove(name)
        persistDoneToday()
        writeProgressInts()
    }

    override fun onEdit(name: String, position: Int) {
        editingIndex = position
        etHabit.setText(name)
        etHabit.setSelection(name.length)
        btnSave.text = "Update"
    }

    override fun onDelete(name: String, position: Int) {
        habits.removeAt(position)
        persistHabits()

        if (name in doneToday) {
            doneToday.remove(name)
            persistDoneToday()
        }

        adapter.notifyItemRemoved(position)
        writeProgressInts()
        Toast.makeText(this, "Deleted \"$name\"", Toast.LENGTH_SHORT).show()

        // If we were editing this row, reset
        if (editingIndex != null && editingIndex == position) {
            editingIndex = null
            btnSave.text = "Add"
            etHabit.text.clear()
        }
    }

    // ==== Persistence helpers ====
    private fun persistHabits() {
        sp.edit().putStringSet("habits_list", habits.toSet()).apply()
    }

    private fun persistDoneToday() {
        sp.edit().putStringSet(todayKey("habits_done_set"), doneToday).apply()
    }

    private fun writeProgressInts() {
        val total = habits.size
        val done = doneToday.size
        sp.edit()
            .putInt(todayKey("habits_total"), total)
            .putInt(todayKey("habits_done"), done)
            .apply()
    }
}
