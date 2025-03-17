package com.devspace.taskbeats.ui.view

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devspace.taskbeats.R
import com.devspace.taskbeats.data.model.TaskSuggestion
import com.devspace.taskbeats.ui.adapter.TaskSuggestionAdapter
import com.devspace.taskbeats.viewmodel.TaskViewModel

/**
 * Diálogo dedicado para mostrar sugestões da IA
 */
class AiSuggestionsDialogFragment : DialogFragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var closeButton: Button
    
    private lateinit var viewModel: TaskViewModel
    private var onSuggestionSelectedListener: ((TaskSuggestion) -> Unit)? = null
    
    private val suggestionAdapter = TaskSuggestionAdapter { suggestion ->
        // Quando uma sugestão for selecionada, notificar o listener e fechar o diálogo
        onSuggestionSelectedListener?.invoke(suggestion)
        dismiss()
    }
    
    companion object {
        fun newInstance(
            viewModel: TaskViewModel,
            query: String,
            categoryName: String,
            onSuggestionSelected: (TaskSuggestion) -> Unit
        ): AiSuggestionsDialogFragment {
            val fragment = AiSuggestionsDialogFragment()
            fragment.viewModel = viewModel
            fragment.onSuggestionSelectedListener = onSuggestionSelected
            
            // Guardar os parâmetros em argumentos
            val args = Bundle()
            args.putString("query", query)
            args.putString("categoryName", categoryName)
            fragment.arguments = args
            
            return fragment
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflar o layout para o diálogo
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_ai_suggestions, null)
        
        // Configurar views e comportamentos
        setupViews(view)
        
        // Criar o diálogo usando AlertDialog.Builder
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }
    
    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.suggestions_dialog_recycler_view)
        progressBar = view.findViewById(R.id.suggestions_dialog_progress_bar)
        emptyText = view.findViewById(R.id.suggestions_dialog_empty_text)
        closeButton = view.findViewById(R.id.suggestions_dialog_btn_close)
        
        // Configurar o RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = suggestionAdapter
        
        // Configurar o botão de fechar
        closeButton.setOnClickListener {
            dismiss()
        }
        
        // Observar as sugestões
        viewModel.taskSuggestions.observe(requireActivity()) { suggestions ->
            updateSuggestions(suggestions)
        }
        
        // Observar o estado de carregamento
        viewModel.isLoadingSuggestions.observe(requireActivity()) { isLoading ->
            progressBar.isVisible = isLoading
            if (isLoading) {
                recyclerView.isVisible = false
                emptyText.isVisible = false
            }
        }
        
        // Solicitar sugestões automaticamente ao abrir o diálogo
        requestSuggestions()
    }
    
    private fun requestSuggestions() {
        val query = arguments?.getString("query") ?: return
        val categoryName = arguments?.getString("categoryName") ?: return
        
        // Solicitar sugestões
        Log.d("AiSuggestionsDialog", "Solicitando sugestões para: $query, categoria: $categoryName")
        viewModel.getTaskSuggestions(query, categoryName)
    }
    
    private fun updateSuggestions(suggestions: List<TaskSuggestion>) {
        val hasSuggestions = suggestions.isNotEmpty()
        
        progressBar.isVisible = false
        recyclerView.isVisible = hasSuggestions
        emptyText.isVisible = !hasSuggestions
        
        suggestionAdapter.submitList(suggestions)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Limpar as sugestões ao fechar o diálogo
        viewModel.clearSuggestions()
    }
} 