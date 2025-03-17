package com.devspace.taskbeats.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devspace.taskbeats.data.local.CategoryEntity
import com.devspace.taskbeats.data.local.TaskEntity
import com.devspace.taskbeats.data.local.TaskWithSubtasks
import com.devspace.taskbeats.data.model.CategoryUiData
import com.devspace.taskbeats.data.model.TaskUiData
import com.devspace.taskbeats.data.model.TaskSuggestion
import com.devspace.taskbeats.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _tasksUiData = MutableLiveData<List<TaskUiData>>()
    val tasksUiData: LiveData<List<TaskUiData>> = _tasksUiData

    private val _categoriesUiData = MutableLiveData<List<CategoryUiData>>()
    val categoriesUiData: LiveData<List<CategoryUiData>> = _categoriesUiData

    private val _selectedCategoryId = MutableLiveData<Long?>(null) // null representa "ALL"
    val selectedCategoryId: LiveData<Long?> = _selectedCategoryId

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _taskSuggestions = MutableLiveData<List<TaskSuggestion>>()
    val taskSuggestions: LiveData<List<TaskSuggestion>> = _taskSuggestions
    
    private val _isLoadingSuggestions = MutableLiveData<Boolean>(false)
    val isLoadingSuggestions: LiveData<Boolean> = _isLoadingSuggestions

    private var categoryMap: Map<Long, CategoryEntity> = emptyMap()
    private var allTasks: List<TaskWithSubtasks> = emptyList()

    init {
        // Observar categorias em tempo real
        repository.getAllCategories().observeForever { categories ->
            Log.d("TaskViewModel", "Categorias carregadas do repository: ${categories.size}")
            categoryMap = categories.associateBy { it.id }
            // Adicionar categoria fixa "ALL" com ID 0
            val allCategory = CategoryUiData(id = 0, name = "ALL", isSelected = _selectedCategoryId.value == null)
            val categoryUiData = listOf(allCategory) + categories.map { category ->
                CategoryUiData(
                    id = category.id,
                    name = category.name,
                    isSelected = _selectedCategoryId.value == category.id
                )
            }
            _categoriesUiData.value = categoryUiData
            Log.d("TaskViewModel", "Atualizando categorias na UI: ${categoryUiData.map { "${it.name} (isSelected: ${it.isSelected})" }}")
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
            // Atualizar o estado isSelected das categorias
            val currentCategories = _categoriesUiData.value ?: return@observeForever
            val updatedCategories = currentCategories.map { category ->
                category.copy(isSelected = category.id == categoryId || (category.id.toInt() == 0 && categoryId == null))
            }
            _categoriesUiData.value = updatedCategories
            filterTasksBySelectedCategory()
        }
    }

    private fun filterTasksBySelectedCategory() {
        val tasksWithSubtasks = allTasks
        Log.d("TaskViewModel", "Tarefas a serem filtradas: ${tasksWithSubtasks.size}")
        val filteredTasks = if (_selectedCategoryId.value == null) {
            tasksWithSubtasks // Mostrar todas as tarefas quando "ALL" está selecionado
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
                // Selecionar a nova categoria automaticamente (exceto "ALL")
                if (categoryId != 0L) _selectedCategoryId.value = categoryId
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    /**
     * Cria uma tarefa com uma categoria existente
     */
    fun createTaskWithExistingCategory(name: String, description: String?, categoryId: Long) {
        viewModelScope.launch {
            try {
                Log.d("TaskViewModel", "Criando tarefa com categoria existente ID: $categoryId")
                // Criar a tarefa com a categoria existente
                repository.createTaskAndGenerateSubtasks(name, description, categoryId)
                // Selecionar a categoria automaticamente
                if (categoryId != 0L) _selectedCategoryId.value = categoryId
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
        Log.d("TaskViewModel", "Selecionando categoria: ${category.name} (ID: ${category.id})")
        val newCategoryId = if (category.id == 0L) null else category.id // "ALL" define como null
        Log.d("TaskViewModel", "Nova categoria selecionada: $newCategoryId (era ${_selectedCategoryId.value})")
        _selectedCategoryId.value = newCategoryId
    }

    /**
     * Obtém sugestões de tarefas da API XAI
     */
    fun getTaskSuggestions(query: String, categoryName: String) {
        _isLoadingSuggestions.value = true
        viewModelScope.launch {
            try {
                val suggestions = repository.getTaskSuggestions(query, categoryName)
                _taskSuggestions.value = suggestions
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Erro ao obter sugestões: ${e.message}", e)
                _errorMessage.value = "Erro ao obter sugestões: ${e.message}"
                _taskSuggestions.value = emptyList()
            } finally {
                _isLoadingSuggestions.value = false
            }
        }
    }
    
    /**
     * Limpa as sugestões atuais
     */
    fun clearSuggestions() {
        _taskSuggestions.value = emptyList()
    }
    
    /**
     * Cria uma tarefa a partir de uma sugestão
     */
    fun createTaskFromSuggestion(suggestion: TaskSuggestion) {
        viewModelScope.launch {
            try {
                // Verificar se a categoria já existe ou criar uma nova
                val categoryId = repository.getCategoryByName(suggestion.category)?.id
                    ?: repository.insertCategory(CategoryEntity(name = suggestion.category))
                
                // Criar a tarefa com a categoria
                val taskWithSubtasks = repository.createTaskAndGenerateSubtasks(
                    name = suggestion.title,
                    description = suggestion.description,
                    categoryId = categoryId
                )
                
                // Selecionar a categoria automaticamente
                if (categoryId != 0L) _selectedCategoryId.value = categoryId
                
                // Limpar as sugestões após criar a tarefa
                clearSuggestions()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    /**
     * Obtém uma tarefa pelo ID
     */
    suspend fun getTaskById(taskId: Long): TaskEntity? {
        return repository.getTaskById(taskId)
    }
    
    /**
     * Exclui uma tarefa
     */
    suspend fun deleteTask(task: TaskEntity) {
        repository.deleteTask(task)
    }
    
    /**
     * Move uma tarefa para a categoria "Tarefas Realizadas"
     */
    suspend fun moveTaskToCompletedCategory(taskId: Long): Boolean {
        return repository.moveTaskToCompletedCategory(taskId)
    }
}