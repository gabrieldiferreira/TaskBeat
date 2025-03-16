package com.devspace.taskbeats.ui.view

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devspace.taskbeats.R
import com.devspace.taskbeats.data.model.SubTaskUiData
import com.devspace.taskbeats.data.remote.ApiClient
import com.devspace.taskbeats.repository.TaskRepository
import com.devspace.taskbeats.ui.adapter.CategoryListAdapter
import com.devspace.taskbeats.ui.adapter.SubTaskListAdapter
import com.devspace.taskbeats.ui.adapter.TaskListAdapter
import com.devspace.taskbeats.viewmodel.TaskViewModel
import com.devspace.taskbeats.viewmodel.TaskViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: TaskViewModel
    private lateinit var taskAdapter: TaskListAdapter
    private lateinit var subTaskAdapter: SubTaskListAdapter
    private lateinit var categoryAdapter: CategoryListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvCategory = findViewById<RecyclerView>(R.id.rv_categories)
        val rvTask = findViewById<RecyclerView>(R.id.rv_tasks)
        val rvSubTasks = findViewById<RecyclerView>(R.id.rv_subtasks)
        val fab = findViewById<FloatingActionButton>(R.id.fab)

        // Inicializar o viewModel primeiro
        val repository = TaskRepository.create(
            context = this, 
            openAiService = ApiClient.openAiService,
            xaiService = ApiClient.xaiService
        )
        viewModel = ViewModelProvider(this, TaskViewModelFactory(repository))
            .get(TaskViewModel::class.java)

        // Agora inicializar os adaptadores
        taskAdapter = TaskListAdapter()
        categoryAdapter = CategoryListAdapter(viewModel)
        subTaskAdapter = SubTaskListAdapter()

        rvCategory.adapter = categoryAdapter
        rvCategory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rvTask.adapter = taskAdapter
        rvTask.layoutManager = LinearLayoutManager(this)

        rvSubTasks.adapter = subTaskAdapter
        rvSubTasks.layoutManager = LinearLayoutManager(this)

        // Observar mudanças nos dados
        viewModel.tasksUiData.observe(this) { tasks ->
            Log.d("MainActivity", "Tarefas recebidas para exibição: ${tasks?.size ?: 0}")
            if (tasks.isNullOrEmpty()) {
                Log.w("MainActivity", "Lista de tarefas está vazia ou nula")
            } else {
                tasks.forEach { task ->
                    Log.d("MainActivity", "Tarefa: ${task.name}, Categoria: ${task.categoryName}, ID: ${task.id}")
                }
            }
            taskAdapter.submitList(tasks)
            val expandedTask = tasks?.find { it.isExpanded }
            rvSubTasks.visibility = if (expandedTask != null) View.VISIBLE else View.GONE
            if (expandedTask != null) {
                val subTasks = repository.getSubtasksForTask(expandedTask.id).value?.map { subTask ->
                    SubTaskUiData(
                        id = subTask.id,
                        taskId = subTask.taskId,
                        name = subTask.title,
                        isCompleted = subTask.isCompleted
                    )
                } ?: emptyList()
                Log.d("MainActivity", "Subtarefas para tarefa ${expandedTask.id}: ${subTasks.size}")
                subTaskAdapter.submitList(subTasks)
            }
        }

        viewModel.categoriesUiData.observe(this) { categories ->
            Log.d("MainActivity", "Categorias recebidas: ${categories?.size ?: 0}")
            if (categories.isNullOrEmpty()) {
                Log.w("MainActivity", "Lista de categorias está vazia ou nula")
            } else {
                categories.forEach { category ->
                    Log.d("MainActivity", "Categoria: ${category.name}, ID: ${category.id}")
                }
            }
            categoryAdapter.submitList(categories)
        }

        viewModel.errorMessage.observe(this) { error ->
            Log.e("MainActivity", "Erro: $error")
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }

        // Configurar listeners
        taskAdapter.setOnClickListener { task ->
            viewModel.onTaskClicked(task)
        }
        
        // Configurar listener para excluir tarefas
        taskAdapter.setOnDeleteListener { task ->
            confirmDeleteTask(task.id, task.name)
        }
        
        // Configurar listener para marcar tarefa como concluída
        taskAdapter.setOnCompleteListener { task ->
            if (!task.isCompleted) {
                moveTaskToCompleted(task.id)
            }
        }

        categoryAdapter.setOnClickListener { category ->
            viewModel.onCategorySelected(category)
        }

        fab.setOnClickListener {
            val bottomSheet = BottomSheetAddTaskFragment(viewModel)
            bottomSheet.show(supportFragmentManager, "BottomSheetAddTaskFragment")
        }
    }
    
    /**
     * Mostra diálogo de confirmação para excluir uma tarefa
     */
    private fun confirmDeleteTask(taskId: Long, taskName: String) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Tarefa")
            .setMessage("Tem certeza que deseja excluir a tarefa '$taskName'?")
            .setPositiveButton("Sim") { _, _ ->
                deleteTask(taskId)
            }
            .setNegativeButton("Não", null)
            .show()
    }
    
    /**
     * Exclui uma tarefa pelo ID
     */
    private fun deleteTask(taskId: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val task = viewModel.getTaskById(taskId)
                if (task != null) {
                    viewModel.deleteTask(task)
                    Toast.makeText(this@MainActivity, "Tarefa excluída com sucesso", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erro ao excluir tarefa: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Move uma tarefa para a categoria "Tarefas Realizadas"
     */
    private fun moveTaskToCompleted(taskId: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val success = viewModel.moveTaskToCompletedCategory(taskId)
                if (success) {
                    Toast.makeText(this@MainActivity, "Tarefa concluída e movida para 'Tarefas Realizadas'", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erro ao marcar tarefa como concluída: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}