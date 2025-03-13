package com.devspace.taskbeats.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.devspace.taskbeats.R
import com.devspace.taskbeats.viewmodel.TaskViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetAddTaskFragment(
    private val viewModel: TaskViewModel
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskNameInput = view.findViewById<EditText>(R.id.task_name_input)
        val categoryNameInput = view.findViewById<EditText>(R.id.category_name_input)
        val addButton = view.findViewById<Button>(R.id.btn_add_task)

        addButton.setOnClickListener {
            val name = taskNameInput.text.toString()
            val categoryName = categoryNameInput.text.toString()

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "O nome da tarefa não pode estar vazio.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (categoryName.isBlank()) {
                Toast.makeText(requireContext(), "O nome da categoria não pode estar vazio.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Criar a tarefa com a nova categoria
            viewModel.createTaskWithNewCategory(name, null, categoryName)
            dismiss()
        }
    }
}