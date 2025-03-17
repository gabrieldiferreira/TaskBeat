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
import com.devspace.taskbeats.data.model.TaskContext
import com.devspace.taskbeats.data.model.TaskSuggestion
import com.devspace.taskbeats.data.model.XaiRequest
import com.devspace.taskbeats.data.remote.XaiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class TaskRepository(
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao,
    private val openAiService: XaiService,
    private val xaiService: XaiService
) {

    companion object {
        fun create(context: Context, openAiService: XaiService, xaiService: XaiService): TaskRepository {
            val database = DatabaseProvider.getDatabase(context)
            return TaskRepository(
                taskDao = database.getTaskDao(),
                categoryDao = database.getCategoryDao(),
                openAiService = openAiService,
                xaiService = xaiService
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

    // Retornar uma categoria pelo nome
    suspend fun getCategoryByName(name: String): CategoryEntity? = withContext(Dispatchers.IO) {
        val category = categoryDao.getByName(name)
        Log.d("TaskRepository", "Categoria recuperada pelo nome '$name': $category")
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
        val existingTask = taskDao.getTaskWithSubtasksById(taskId)
        if (existingTask != null && existingTask.subtasks.isNotEmpty()) {
            Log.d("TaskRepository", "Subtarefas já existem para a tarefa $taskId. Usando dados salvos.")
            return@withContext existingTask
        }


        // Gerar subtarefas com a API
        val category = categoryDao.getById(categoryId)?.name ?: "Geral"
        val prompt = """
            Você é um assistente de produtividade especializado em tarefas da categoria "$category".
            Sua tarefa é dividir uma tarefa principal em subtarefas específicas, práticas e numeradas.
            Exemplo:
            - Tarefa Principal: "Criar um aplicativo"
            - Subtarefas:
            1. Definir os requisitos do aplicativo
            2. Criar wireframes
            3. Desenvolver a interface
            4. Implementar funcionalidades principais
            5. Testar e corrigir bugs
    
            Agora, para a tarefa "$name", forneça apenas as subtarefas numeradas.
        """.trimIndent()


        val request = XaiRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(
                com.devspace.taskbeats.data.model.Message(
                    role = "user",
                    content = prompt
                )
            ),
            temperature = 0.7 // Para respostas mais previsíveis
        )

        val subtaskEntities = try {
            val response = openAiService.getSuggestions(request)
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

    /**
     * Obtém sugestões de tarefas da API X.AI
     */
    suspend fun getTaskSuggestions(
        query: String,
        categoryName: String,
        maxSuggestions: Int = 5
    ): List<TaskSuggestion> = withContext(Dispatchers.IO) {
        try {
            Log.d("TaskRepository", "Iniciando solicitação de sugestões para query: '$query', categoria: '$categoryName'")
            
            // Obter as últimas tarefas do usuário para personalização
            val recentTasks = taskDao.getRecentTasks(10).map { it.name }
            Log.d("TaskRepository", "Tarefas recentes para contextualização: ${recentTasks.joinToString(", ")}")
            
            // Criar o prompt para a solicitação
            val systemPrompt = """
                Você é um assistente de produtividade especializado em gerenciar tarefas.
                Sua função é gerar sugestões de tarefas baseadas na consulta do usuário.
                Retorne exatamente $maxSuggestions sugestões de tarefas para a categoria "$categoryName".
                
                Cada sugestão deve seguir o formato JSON:
                {
                  "title": "Título da tarefa",
                  "description": "Descrição detalhada da tarefa",
                  "category": "$categoryName"
                }
                
                Retorne apenas as sugestões em formato JSON, sem texto adicional.
            """.trimIndent()
            
            val userPrompt = """
                Preciso de $maxSuggestions sugestões de tarefas para: "$query"
                Categoria: "$categoryName"
                
                Contexto adicional - Minhas tarefas recentes: ${recentTasks.joinToString(", ")}
            """.trimIndent()
            
            // Criar a requisição no formato da API X.AI
            val messages = listOf(
                Message(role = "system", content = systemPrompt),
                Message(role = "user", content = userPrompt)
            )
            
            val request = XaiRequest(
                messages = messages,
                model = "grok-2-latest",
                stream = false,
                temperature = 0.7,
                taskContext = TaskContext(
                    query = query,
                    category = categoryName,
                    previousTasks = recentTasks
                )
            )
            
            Log.d("TaskRepository", "Enviando requisição para API X.AI")
            
            // Fazer a chamada à API
            try {
                Log.d("TaskRepository", "Tentando conexão com api.x.ai...")
                val response = xaiService.getSuggestions(request)
                Log.d("TaskRepository", "Resposta recebida da API. Código: ${response.code()}")
                
                if (!response.isSuccessful) {
                    Log.e("TaskRepository", "Erro ao obter sugestões: ${response.code()} - ${response.message()} - ${response.errorBody()?.string()}")
                    return@withContext getDemoSuggestions(query, categoryName, maxSuggestions)
                }
                
                // Processar a resposta da API X.AI e converter para TaskSuggestion
                val responseBody = response.body()
                if (responseBody == null) {
                    Log.e("TaskRepository", "Corpo da resposta é nulo")
                    return@withContext getDemoSuggestions(query, categoryName, maxSuggestions)
                }
                
                val content = responseBody.choices.firstOrNull()?.message?.content ?: ""
                Log.d("TaskRepository", "Conteúdo da resposta: $content")
                
                // Tentar extrair sugestões do conteúdo (formato JSON)
                val suggestions = try {
                    parseJsonSuggestions(content, categoryName)
                } catch (e: Exception) {
                    Log.e("TaskRepository", "Erro ao analisar sugestões JSON: ${e.message}")
                    getDemoSuggestions(query, categoryName, maxSuggestions)
                }
                
                Log.d("TaskRepository", "Sugestões processadas: ${suggestions.size}")
                return@withContext suggestions
                
            } catch (e: Exception) {
                Log.e("TaskRepository", "Exceção durante a chamada à API: ${e.javaClass.simpleName} - ${e.message}", e)
                return@withContext getDemoSuggestions(query, categoryName, maxSuggestions)
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Erro ao obter sugestões: ${e.message}", e)
            return@withContext getDemoSuggestions(query, categoryName, maxSuggestions)
        }
    }
    
    /**
     * Tenta extrair sugestões do conteúdo JSON da resposta
     */
    private fun parseJsonSuggestions(content: String, defaultCategory: String): List<TaskSuggestion> {
        // Implementação simplificada: extrair manualmente as sugestões do texto
        // Em uma implementação real, usar uma biblioteca JSON para analisar corretamente
        
        // Identificar padrões básicos de título e descrição
        val suggestions = mutableListOf<TaskSuggestion>()
        
        // Pegar blocos entre chaves
        val regex = "\\{[^\\{\\}]*\\}".toRegex()
        val matches = regex.findAll(content)
        
        for ((index, match) in matches.withIndex()) {
            val json = match.value
            
            // Extrair título (simplificado)
            val titleMatch = "\"title\"\\s*:\\s*\"([^\"]*)\"".toRegex().find(json)
            val title = titleMatch?.groupValues?.get(1) ?: "Tarefa ${index + 1}"
            
            // Extrair descrição (simplificado)
            val descMatch = "\"description\"\\s*:\\s*\"([^\"]*)\"".toRegex().find(json)
            val description = descMatch?.groupValues?.get(1)
            
            // Extrair categoria (simplificado)
            val catMatch = "\"category\"\\s*:\\s*\"([^\"]*)\"".toRegex().find(json)
            val category = catMatch?.groupValues?.get(1) ?: defaultCategory
            
            suggestions.add(
                TaskSuggestion(
                    id = "suggest_${index}",
                    title = title,
                    description = description,
                    category = category,
                    confidenceScore = 0.9 - (index * 0.05)
                )
            )
            
            if (suggestions.size >= 5) break // Limitar a 5 sugestões
        }
        
        return suggestions
    }
    
    /**
     * Gera sugestões de demonstração quando a API não está disponível
     */
    private fun getDemoSuggestions(query: String, categoryName: String, maxSuggestions: Int): List<TaskSuggestion> {
        Log.d("TaskRepository", "Gerando sugestões de demonstração para '$query' na categoria '$categoryName'")
        
        // Lista de possíveis sugestões por categoria
        val suggestionsByCategory = mapOf(
            "Trabalho" to listOf(
                TaskSuggestion(
                    id = "demo1",
                    title = "Preparar apresentação sobre $query",
                    description = "Criar slides e material de apoio para apresentação do projeto $query",
                    category = "Trabalho",
                    confidenceScore = 0.95
                ),
                TaskSuggestion(
                    id = "demo2",
                    title = "Agendar reunião sobre $query",
                    description = "Marcar horário com a equipe para discutir o andamento do $query",
                    category = "Trabalho",
                    confidenceScore = 0.9
                ),
                TaskSuggestion(
                    id = "demo3",
                    title = "Revisar documentação do $query",
                    description = "Atualizar a documentação com as últimas alterações do projeto",
                    category = "Trabalho",
                    confidenceScore = 0.85
                )
            ),
            "Estudos" to listOf(
                TaskSuggestion(
                    id = "demo4",
                    title = "Estudar conteúdo sobre $query",
                    description = "Reservar 2 horas para estudar material relacionado a $query",
                    category = "Estudos",
                    confidenceScore = 0.95
                ),
                TaskSuggestion(
                    id = "demo5",
                    title = "Fazer exercícios de $query",
                    description = "Praticar com exercícios para fixar o conteúdo aprendido",
                    category = "Estudos",
                    confidenceScore = 0.9
                ),
                TaskSuggestion(
                    id = "demo6",
                    title = "Assistir aula sobre $query",
                    description = "Assistir videoaula para complementar o conhecimento",
                    category = "Estudos",
                    confidenceScore = 0.85
                )
            ),
            "Casa" to listOf(
                TaskSuggestion(
                    id = "demo7",
                    title = "Organizar $query",
                    description = "Separar e organizar itens relacionados a $query",
                    category = "Casa",
                    confidenceScore = 0.95
                ),
                TaskSuggestion(
                    id = "demo8",
                    title = "Limpar área de $query",
                    description = "Limpar e organizar o espaço destinado a $query",
                    category = "Casa",
                    confidenceScore = 0.9
                ),
                TaskSuggestion(
                    id = "demo9",
                    title = "Comprar itens para $query",
                    description = "Fazer lista e comprar itens necessários para $query",
                    category = "Casa",
                    confidenceScore = 0.85
                )
            ),
            "Tarefas Realizadas" to listOf(
                TaskSuggestion(
                    id = "demo10",
                    title = "Revisão de $query concluído",
                    description = "Verificar se o trabalho de $query foi concluído corretamente",
                    category = "Tarefas Realizadas",
                    confidenceScore = 0.95
                ),
                TaskSuggestion(
                    id = "demo11",
                    title = "Documentar conclusão de $query",
                    description = "Registrar os resultados do trabalho de $query que foi concluído",
                    category = "Tarefas Realizadas",
                    confidenceScore = 0.9
                )
            )
        )
        
        // Sugestões genéricas para qualquer categoria não mapeada
        val genericSuggestions = listOf(
            TaskSuggestion(
                id = "demo10",
                title = "Planejar $query",
                description = "Criar um plano detalhado para $query",
                category = categoryName,
                confidenceScore = 0.95
            ),
            TaskSuggestion(
                id = "demo11",
                title = "Pesquisar sobre $query",
                description = "Buscar mais informações relacionadas a $query",
                category = categoryName,
                confidenceScore = 0.9
            ),
            TaskSuggestion(
                id = "demo12",
                title = "Organizar ideias sobre $query",
                description = "Fazer brainstorming e organizar ideias relacionadas a $query",
                category = categoryName,
                confidenceScore = 0.85
            ),
            TaskSuggestion(
                id = "demo13",
                title = "Implementar $query",
                description = "Colocar em prática o planejamento de $query",
                category = categoryName,
                confidenceScore = 0.8
            ),
            TaskSuggestion(
                id = "demo14",
                title = "Revisar progresso de $query",
                description = "Analisar o andamento e fazer ajustes necessários em $query",
                category = categoryName,
                confidenceScore = 0.75
            )
        )
        
        // Usar sugestões específicas da categoria ou genéricas se não existirem
        val suggestions = suggestionsByCategory[categoryName]?.take(maxSuggestions) 
            ?: genericSuggestions.take(maxSuggestions)
        
        Log.d("TaskRepository", "Sugestões de demonstração geradas: ${suggestions.size}")
        return suggestions
    }
    
    /**
     * Move uma tarefa para a categoria "Tarefas Realizadas" quando marcada como concluída
     */
    suspend fun moveTaskToCompletedCategory(taskId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            // Buscar a tarefa atual
            val task = taskDao.getTaskById(taskId) ?: return@withContext false
            
            // Verificar se já está na categoria correta
            val currentCategory = categoryDao.getById(task.categoryId)
            if (currentCategory?.name == "Tarefas Realizadas") {
                return@withContext true // Já está na categoria correta
            }
            
            // Buscar ou criar a categoria "Tarefas Realizadas"
            var completedCategory = categoryDao.getByName("Tarefas Realizadas")
            if (completedCategory == null) {
                val newCategory = CategoryEntity(name = "Tarefas Realizadas")
                val newCategoryId = categoryDao.insert(newCategory)
                completedCategory = newCategory.copy(id = newCategoryId)
            }
                
            // Atualizar a tarefa para a nova categoria
            val updatedTask = task.copy(
                categoryId = completedCategory.id,
                isCompleted = true
            )
            
            taskDao.update(updatedTask)
            Log.d("TaskRepository", "Tarefa #$taskId movida para categoria 'Tarefas Realizadas'")
            return@withContext true
            
        } catch (e: Exception) {
            Log.e("TaskRepository", "Erro ao mover tarefa para 'Concluídas': ${e.message}")
            return@withContext false
        }
    }

    // Retornar uma tarefa específica pelo ID
    suspend fun getTaskById(taskId: Long): TaskEntity? = withContext(Dispatchers.IO) {
        val task = taskDao.getTaskById(taskId)
        Log.d("TaskRepository", "Tarefa recuperada pelo ID $taskId: $task")
        task
    }
}