package com.example.chibihabits

import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView

class HabitAdapter(
    private val items: MutableList<String>,
    private val doneToday: Set<String>,
    private val callbacks: HabitCallbacks
) : RecyclerView.Adapter<HabitAdapter.HabitVH>() {

    interface HabitCallbacks {
        fun onToggle(name: String, isChecked: Boolean, position: Int)
        fun onEdit(name: String, position: Int)
        fun onDelete(name: String, position: Int)
    }

    inner class HabitVH(view: View) : RecyclerView.ViewHolder(view) {
        val cb: CheckBox = view.findViewById(R.id.cbHabit)
        val tv: TextView = view.findViewById(R.id.tvHabit)
        val btnMore: ImageButton = view.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitVH(v)
    }

    override fun onBindViewHolder(holder: HabitVH, position: Int) {
        val name = items[position]
        holder.tv.text = name

        // Avoid triggering toggle listener during bind
        holder.cb.setOnCheckedChangeListener(null)
        holder.cb.isChecked = doneToday.contains(name)

        holder.cb.setOnCheckedChangeListener { _, isChecked ->
            callbacks.onToggle(name, isChecked, holder.adapterPosition)
        }

        holder.itemView.setOnClickListener {
            holder.cb.isChecked = !holder.cb.isChecked
        }

        holder.itemView.setOnLongClickListener {
            showPopup(holder.btnMore, holder.adapterPosition)
            true
        }

        holder.btnMore.setOnClickListener {
            showPopup(holder.btnMore, holder.adapterPosition)
        }
    }

    private fun showPopup(anchor: View, position: Int) {
        val context = anchor.context
        val popup = PopupMenu(context, anchor)
        MenuInflater(context).inflate(R.menu.menu_habit_row, popup.menu)
        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.action_edit -> callbacks.onEdit(items[position], position)
                R.id.action_delete -> callbacks.onDelete(items[position], position)
            }
            true
        }
        popup.show()
    }

    override fun getItemCount(): Int = items.size
}
