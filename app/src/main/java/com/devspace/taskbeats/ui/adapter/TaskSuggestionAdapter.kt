package com.devspace.taskbeats.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devspace.taskbeats.R
import com.devspace.taskbeats.data.model.xai.TaskSuggestion

/**
 * Adaptador para exibir sugestões de tarefas da API XAI
 */
class TaskSuggestionAdapter(
    private val onSuggestionClick: (TaskSuggestion) -> Unit
) : ListAdapter<TaskSuggestion, TaskSuggestionAdapter.SuggestionViewHolder>(SuggestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = getItem(position)
        holder.bind(suggestion)
    }

    inner class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.suggestion_title)
        private val categoryTextView: TextView = itemView.findViewById(R.id.suggestion_category)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.suggestion_description)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSuggestionClick(getItem(position))
                }
            }
        }

        fun bind(suggestion: TaskSuggestion) {
            titleTextView.text = suggestion.title
            categoryTextView.text = suggestion.category
            
            if (suggestion.description.isNullOrEmpty()) {
                descriptionTextView.visibility = View.GONE
            } else {
                descriptionTextView.visibility = View.VISIBLE
                descriptionTextView.text = suggestion.description
            }
        }
    }

    private class SuggestionDiffCallback : DiffUtil.ItemCallback<TaskSuggestion>() {
        override fun areItemsTheSame(oldItem: TaskSuggestion, newItem: TaskSuggestion): Boolean {
            return oldItem.title == newItem.title && oldItem.category == newItem.category
        }

        override fun areContentsTheSame(oldItem: TaskSuggestion, newItem: TaskSuggestion): Boolean {
            return oldItem == newItem
        }
    }
} 