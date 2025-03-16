package com.devspace.taskbeats.ui.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.devspace.taskbeats.R
import com.devspace.taskbeats.data.local.CategoryEntity
import com.devspace.taskbeats.ui.adapter.CategorySpinnerAdapter
import com.devspace.taskbeats.viewmodel.TaskViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.widget.Spinner

class BottomSheetAddTaskFragment(
    private val viewModel: TaskViewModel
) : BottomSheetDialogFragment() {

    private lateinit var taskNameInput: EditText
    private lateinit var categoryNameInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var getSuggestionsButton: Button
    private lateinit var addButton: Button
    
    private var selectedCategoryId: Long? = null
    private var selectedCategoryName: String? = null
    private var categories = listOf<CategoryEntity>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskNameInput = view.findViewById(R.id.task_name_input)
        categoryNameInput = view.findViewById(R.id.category_name_input)
        categorySpinner = view.findViewById(R.id.category_spinner)
        getSuggestionsButton = view.findViewById(R.id.btn_get_suggestions)
        addButton = view.findViewById(R.id.btn_add_task)
        
        // Observar as categorias
        viewModel.categoriesUiData.observe(viewLifecycleOwner) { categoryUiDataList ->
            // Converter CategoryUiData para CategoryEntity
            categories = categoryUiDataList
                .filter { it.id != 0L } // Filtrar a categoria "ALL"
                .map { 
                    CategoryEntity(id = it.id, name = it.name) 
                }
            
            setupCategorySpinner()
        }

        // Configurar o spinner de categorias
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    selectedCategoryId = id
                    selectedCategoryName = categories[position - 1].name
                } else {
                    selectedCategoryId = null
                    selectedCategoryName = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategoryId = null
                selectedCategoryName = null
            }
        }

        // Adicionar listener para o campo de nova categoria
        categoryNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val categoryName = s.toString().trim()
                if (categoryName.isNotEmpty()) {
                    // Se o usuário está digitando uma nova categoria, desselecionar o spinner
                    categorySpinner.setSelection(0)
                    selectedCategoryName = categoryName
                }
            }
        })
        
        // Configurar o botão de sugestões
        getSuggestionsButton.setOnClickListener {
            showSuggestionsDialog()
        }

        // Configurar o botão de adicionar tarefa
        addButton.setOnClickListener {
            val name = taskNameInput.text.toString().trim()
            val newCategoryName = categoryNameInput.text.toString().trim()

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "O nome da tarefa não pode estar vazio.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificar se uma categoria foi selecionada ou se uma nova categoria foi digitada
            if (selectedCategoryId != null) {
                // Criar a tarefa com a categoria selecionada
                viewModel.createTaskWithExistingCategory(name, null, selectedCategoryId!!)
            } else if (newCategoryName.isNotBlank()) {
                // Criar a tarefa com a nova categoria
                viewModel.createTaskWithNewCategory(name, null, newCategoryName)
            } else {
                Toast.makeText(requireContext(), "Selecione uma categoria ou crie uma nova.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            dismiss()
        }
    }
    
    private fun setupCategorySpinner() {
        val adapter = CategorySpinnerAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        categorySpinner.adapter = adapter
    }
    
    private fun showSuggestionsDialog() {
        val query = taskNameInput.text.toString().trim()
        val categoryName = selectedCategoryName ?: categoryNameInput.text.toString().trim()
        
        // Validar entrada
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Digite o nome da tarefa para obter sugestões.", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (query.length < 3) {
            Toast.makeText(requireContext(), "Digite pelo menos 3 caracteres para obter sugestões.", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (categoryName.isEmpty()) {
            Toast.makeText(requireContext(), "Selecione ou crie uma categoria para obter sugestões.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Abrir o diálogo de sugestões
        AiSuggestionsDialogFragment.newInstance(
            viewModel = viewModel,
            query = query,
            categoryName = categoryName,
            onSuggestionSelected = { suggestion ->
                // Quando uma sugestão for selecionada no diálogo
                viewModel.createTaskFromSuggestion(suggestion)
                dismiss() // Fechar o BottomSheet após criar a tarefa
            }
        ).show(parentFragmentManager, "suggestions_dialog")
    }
}