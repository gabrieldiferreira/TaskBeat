package com.devspace.taskbeats.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.devspace.taskbeats.R
import com.devspace.taskbeats.data.local.CategoryEntity

/**
 * Adaptador personalizado para o spinner de categorias
 */
class CategorySpinnerAdapter(
    context: Context,
    resource: Int,
    private val categories: List<CategoryEntity>
) : ArrayAdapter<CategoryEntity>(context, resource, categories) {
    
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    
    override fun getCount(): Int {
        // Adiciona +1 para a opção "Selecione uma categoria"
        return categories.size + 1
    }
    
    override fun getItem(position: Int): CategoryEntity? {
        // A posição 0 é reservada para "Selecione uma categoria"
        return if (position == 0) null else categories[position - 1]
    }
    
    override fun getItemId(position: Int): Long {
        // A posição 0 é reservada para "Selecione uma categoria"
        return if (position == 0) -1 else categories[position - 1].id
    }
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.item_spinner_category, parent, false)
        val textView = view.findViewById<TextView>(R.id.tv_spinner_category)
        
        // A posição 0 é reservada para "Selecione uma categoria"
        if (position == 0) {
            textView.text = "Selecione uma categoria"
        } else {
            textView.text = categories[position - 1].name
        }
        
        return view
    }
    
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }
} 