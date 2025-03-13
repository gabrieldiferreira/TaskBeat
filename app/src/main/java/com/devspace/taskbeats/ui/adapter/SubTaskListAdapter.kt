package com.devspace.taskbeats.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devspace.taskbeats.R
import com.devspace.taskbeats.data.model.SubTaskUiData

/**
 * Adaptador para exibir uma lista de subtarefas na UI.
 */
class SubTaskListAdapter : ListAdapter<SubTaskUiData, SubTaskListAdapter.SubTaskViewHolder>(SubTaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubTaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subtask, parent, false)
        return SubTaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubTaskViewHolder, position: Int) {
        val subTask = getItem(position)
        holder.bind(subTask)
    }

    class SubTaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvSubTask = view.findViewById<TextView>(R.id.tv_subtask_title)

        fun bind(subTask: SubTaskUiData) {
            tvSubTask.text = subTask.name
            tvSubTask.paint.isStrikeThruText = subTask.isCompleted
        }
    }

    class SubTaskDiffCallback : DiffUtil.ItemCallback<SubTaskUiData>() {
        override fun areItemsTheSame(oldItem: SubTaskUiData, newItem: SubTaskUiData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SubTaskUiData, newItem: SubTaskUiData): Boolean {
            return oldItem.taskId == newItem.taskId && oldItem.isCompleted == newItem.isCompleted
        }
    }
}