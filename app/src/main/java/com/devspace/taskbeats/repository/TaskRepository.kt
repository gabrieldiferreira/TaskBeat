package com.devspace.taskbeats.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.devspace.taskbeats.data.local.CategoryDao
import com.devspace.taskbeats.data.local.CategoryEntity
import com.devspace.taskbeats.data.local.DatabaseProvider
import com.devspace.taskbeats.data.local.SubTaskEntity
import com.devspace.taskbeats.data.local.TaskDao
import com.devspace.taskbeats.data.local.TaskEntity
import com.devspace.taskbeats.data.local.TaskWithSubtasks
import com.devspace.taskbeats.data.model.Message
import com.devspace.taskbeats.data.model.OpenAiRequest
import com.devspace.taskbeats.data.model.Role
import com.devspace.taskbeats.data.remote.OpenAiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository(
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao,
    private val openAiService: OpenAiService
) {

    companion object {
        fun create(context: Context, openAiService: OpenAiService): TaskRepository {
            val database = DatabaseProvider.getDatabase(context)
            return TaskRepository(
                taskDao = database.getTaskDao(),
                categoryDao = database.getCategoryDao(),
                openAiService = openAiService
            )
        }
    }

    // Inserir uma nova categoria
    suspend fun insertCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        val categoryId = categoryDao.insert(category)
        Log.d("TaskRepository", "Categoria inserida com ID: $categoryId, LiveData atualizado")
        categoryId
    }

    // Inserir uma nova tarefa
    suspend fun insertTask(task: TaskEntity): Long = withContext(Dispatchers.IO) {
        val taskId = taskDao.insert(task)
        Log.d("TaskRepository", "Tarefa inserida com ID: $taskId, LiveData atualizado")
        taskId
    }

    // Inserir uma lista de subtarefas
    suspend fun insertSubtasks(subtasks: List<SubTaskEntity>) = withContext(Dispatchers.IO) {
        taskDao.insertSubtasks(subtasks)
        Log.d("TaskRepository", "Subtarefas inseridas: ${subtasks.size}, LiveData atualizado")
    }

    // Atualizar uma tarefa existente
    suspend fun updateTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.update(task)
        Log.d("TaskRepository", "Tarefa atualizada: ${task.id}, LiveData atualizado")
    }

    // Excluir uma tarefa (as subtarefas serão excluídas em cascata pelo Room)
    suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        taskDao.delete(task)
        Log.d("TaskRepository", "Tarefa excluída: ${task.id}, LiveData atualizado")
    }

    // Retornar todas as tarefas com suas subtarefas
    fun getAllTasks(): LiveData<List<TaskWithSubtasks>> {
        return taskDao.getTasksWithSubtasks()
    }

    // Retornar tarefas por categoria
    fun getTasksByCategory(categoryId: Long): LiveData<List<TaskEntity>> {
        return taskDao.getTasksByCategory(categoryId)
    }

    // Retornar subtarefas de uma tarefa específica
    fun getSubtasksForTask(taskId: Long): LiveData<List<SubTaskEntity>> {
        return taskDao.getSubtasksForTask(taskId)
    }

    // Retornar uma categoria por ID
    suspend fun getCategoryById(categoryId: Long): CategoryEntity? = withContext(Dispatchers.IO) {
        val category = categoryDao.getById(categoryId)
        Log.d("TaskRepository", "Categoria recuperada pelo ID $categoryId: $category")
        category
    }

    // Retornar todas as categorias
    fun getAllCategories(): LiveData<List<CategoryEntity>> {
        return categoryDao.getAll()
    }

    // Criar uma tarefa e gerar subtarefas com a API do ChatGPT
    suspend fun createTaskAndGenerateSubtasks(
        name: String,
        description: String?,
        categoryId: Long
    ): TaskWithSubtasks = withContext(Dispatchers.IO) {
        // Validação básica
        require(name.isNotBlank()) { "O nome da tarefa não pode estar vazio." }
        require(categoryDao.getById(categoryId) != null) { "A categoria selecionada não existe." }

        // Inserir a tarefa
        val task = TaskEntity(name = name,  categoryId = categoryId)
        val taskId = taskDao.insert(task)
        Log.d("TaskRepository", "Tarefa inserida com ID: $taskId, LiveData atualizado")

        // Verificar se já existem subtarefas salvas para essa tarefa
        val existingSubtasks = taskDao.getSubtasksForTask(taskId).value
        if (!existingSubtasks.isNullOrEmpty()) {
            Log.d("TaskRepository", "Subtarefas já existem para a tarefa $taskId. Usando dados salvos.")
            return@withContext taskDao.getTaskWithSubtasksById(taskId)
                ?: throw Exception("Tarefa não encontrada após inserção (subtarefas existentes)")
        }

        // Gerar subtarefas com a API
        val category = categoryDao.getById(categoryId)?.name ?: "Geral"
        val prompt = """
            Você é um assistente de produtividade especializado em tarefas da categoria "$category".
            Sua tarefa é dividir uma tarefa principal em subtarefas específicas, práticas e numeradas.
            Liste apenas as subtarefas, sem explicações adicionais.
            Para a tarefa principal "$name", forneça as subtarefas numeradas.
        """.trimIndent()

        val request = OpenAiRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(Message(role = Role.ASSISTANT, content = prompt)),
            max_tokens = 150,
            temperature = 0.7 // Para respostas mais previsíveis
        )

        val subtaskEntities = try {
            val response = openAiService.generateSubTasks(request)
            if (!response.isSuccessful) {
                when (response.code()) {
                    429 -> {
                        Log.w("TaskRepository", "Limite de requisições atingido: ${response.code()} - ${response.message()}")
                        emptyList() // Retorna lista vazia para usar dados salvos
                    }
                    401 -> throw Exception("API Key inválida. Verifique sua configuração.")
                    else -> throw Exception("Erro ao chamar a API: ${response.code()} - ${response.message()}")
                }
            } else {
                val body = response.body() ?: throw Exception("Resposta da API é nula")
                val rawResponse = body.choices.first().message.content
                rawResponse.lines().map { it.trim() }.filter { it.isNotBlank() }.map { subtaskTitle ->
                    SubTaskEntity(taskId = taskId, title = subtaskTitle)
                }
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Erro na API: ${e.message}")
            emptyList() // Retorna lista vazia em caso de falha
        }

        // Inserir as subtarefas no banco se a API retornou resultados
        if (subtaskEntities.isNotEmpty()) {
            taskDao.insertSubtasks(subtaskEntities)
            Log.d("TaskRepository", "Subtarefas inseridas: ${subtaskEntities.size}, LiveData atualizado")
        } else {
            Log.w("TaskRepository", "Nenhuma subtarefa gerada pela API. Usando dados salvos, se disponíveis.")
        }

        // Recuperar a tarefa com suas subtarefas diretamente
        val taskWithSubtasks = taskDao.getTaskWithSubtasksById(taskId)
        Log.d("TaskRepository", "Tarefa recuperada após inserção: $taskWithSubtasks, LiveData atualizado")
        return@withContext taskWithSubtasks
            ?: throw Exception("Tarefa não encontrada após inserção (final)")
    }
}