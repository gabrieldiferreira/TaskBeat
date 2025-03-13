package com.devspace.taskbeats.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devspace.taskbeats.data.local.CategoryEntity
import com.devspace.taskbeats.data.local.TaskWithSubtasks
import com.devspace.taskbeats.data.model.CategoryUiData
import com.devspace.taskbeats.data.model.TaskUiData
import com.devspace.taskbeats.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _tasksUiData = MutableLiveData<List<TaskUiData>>()
    val tasksUiData: LiveData<List<TaskUiData>> = _tasksUiData

    private val _categoriesUiData = MutableLiveData<List<CategoryUiData>>()
    val categoriesUiData: LiveData<List<CategoryUiData>> = _categoriesUiData

    private val _selectedCategoryId = MutableLiveData<Long?>(null)
    val selectedCategoryId: LiveData<Long?> = _selectedCategoryId

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var categoryMap: Map<Long, CategoryEntity> = emptyMap()
    private var allTasks: List<TaskWithSubtasks> = emptyList()

    init {
        // Observar categorias em tempo real
        repository.getAllCategories().observeForever { categories ->
            Log.d("TaskViewModel", "Categorias carregadas: ${categories.size}")
            categoryMap = categories.associateBy { it.id }
            val categoryUiData = categories.map { category ->
                CategoryUiData(
                    id = category.id,
                    name = category.name,
                    isSelected = _selectedCategoryId.value == category.id
                )
            }
            _categoriesUiData.value = categoryUiData
            filterTasksBySelectedCategory()
        }

        // Observar tarefas em tempo real
        repository.getAllTasks().observeForever { tasksWithSubtasks ->
            Log.d("TaskViewModel", "Tarefas carregadas do Room: ${tasksWithSubtasks.size}")
            allTasks = tasksWithSubtasks
            filterTasksBySelectedCategory()
        }

        // Observar mudanças na categoria selecionada
        _selectedCategoryId.observeForever { categoryId ->
            Log.d("TaskViewModel", "Categoria selecionada alterada para: $categoryId")
            filterTasksBySelectedCategory()
        }
    }

    private fun filterTasksBySelectedCategory() {
        val tasksWithSubtasks = allTasks
        Log.d("TaskViewModel", "Tarefas a serem filtradas: ${tasksWithSubtasks.size}")
        val filteredTasks = if (_selectedCategoryId.value == null) {
            tasksWithSubtasks // Mostrar todas as tarefas se nenhuma categoria estiver selecionada
        } else {
            tasksWithSubtasks.filter { it.task.categoryId == _selectedCategoryId.value }
        }
        val tasksUiData = filteredTasks.map { taskWithSubtasks ->
            val category = categoryMap[taskWithSubtasks.task.categoryId] ?: run {
                Log.w("TaskViewModel", "Categoria não encontrada para taskId: ${taskWithSubtasks.task.id}, categoryId: ${taskWithSubtasks.task.categoryId}")
                CategoryEntity(id = 0, name = "Geral")
            }
            TaskUiData(
                id = taskWithSubtasks.task.id,
                name = taskWithSubtasks.task.name,
                categoryName = category.name,
                isCompleted = taskWithSubtasks.task.isCompleted,
                isExpanded = false
            )
        }
        Log.d("TaskViewModel", "Tarefas mapeadas para UI: ${tasksUiData.size}")
        _tasksUiData.value = tasksUiData
    }

    fun createTaskWithNewCategory(name: String, description: String?, categoryName: String) {
        viewModelScope.launch {
            try {
                // Criar a nova categoria
                val categoryId = repository.insertCategory(CategoryEntity(name = categoryName))
                Log.d("TaskViewModel", "Nova categoria criada com ID: $categoryId")
                // Criar a tarefa com a nova categoria
                repository.createTaskAndGenerateSubtasks(name, description, categoryId)
                // Selecionar a nova categoria automaticamente
                _selectedCategoryId.value = categoryId
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun onTaskClicked(task: TaskUiData) {
        val currentTasks = _tasksUiData.value ?: return
        val updatedTasks = currentTasks.map { it.copy(isExpanded = it.id == task.id && !task.isExpanded) }
        _tasksUiData.value = updatedTasks
    }

    fun onCategorySelected(category: CategoryUiData) {
        _selectedCategoryId.value = category.id
        val currentCategories = _categoriesUiData.value ?: return
        val updatedCategories = currentCategories.map { it.copy(isSelected = it.id == category.id) }
        _categoriesUiData.value = updatedCategories
    }
}