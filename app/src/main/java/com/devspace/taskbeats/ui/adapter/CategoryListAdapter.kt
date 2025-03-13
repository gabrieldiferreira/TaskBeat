package com.devspace.taskbeats.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devspace.taskbeats.R
import com.devspace.taskbeats.data.model.CategoryUiData
import com.devspace.taskbeats.viewmodel.TaskViewModel

/**
 * Adaptador para exibir uma lista de categorias na UI.
 */
class CategoryListAdapter(
    private val viewModel: TaskViewModel
) : ListAdapter<CategoryUiData, CategoryListAdapter.CategoryViewHolder>(CategoryDiffCallback) {

    private var onClick: ((CategoryUiData) -> Unit)? = null

    fun setOnClickListener(onClick: (CategoryUiData) -> Unit) {
        this.onClick = onClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category)
    }

    inner class CategoryViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val tvCategory = view.findViewById<TextView>(R.id.tv_category)

        fun bind(category: CategoryUiData) {
            tvCategory.text = category.name
            tvCategory.isSelected = category.isSelected

            view.setOnClickListener {
                viewModel.onCategorySelected(category)
                onClick?.invoke(category)
            }
        }
    }

    companion object CategoryDiffCallback : DiffUtil.ItemCallback<CategoryUiData>() {
        override fun areItemsTheSame(oldItem: CategoryUiData, newItem: CategoryUiData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryUiData, newItem: CategoryUiData): Boolean {
            return oldItem.name == newItem.name && oldItem.isSelected == newItem.isSelected
        }
    }
}