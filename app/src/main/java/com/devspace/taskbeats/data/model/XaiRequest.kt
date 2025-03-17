package com.devspace.taskbeats.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de requisição para a API X.AI
 */
data class XaiRequest(
    val messages: List<XaiMessage>,
    val model: String = "grok-2-latest",
    val stream: Boolean = false,
    val temperature: Double = 0.7,
    
    // Campos adicionais para metadados
    @SerializedName("task_context")
    val taskContext: TaskContext? = null
)

/**
 * Representa uma mensagem no formato requerido pela API X.AI
 */
data class XaiMessage(
    val role: String,
    val content: String
)

/**
 * Contexto adicional da tarefa
 */
data class TaskContext(
    val query: String,
    val category: String,
    val previousTasks: List<String> = emptyList()
)

/**
 * Preferências do usuário para personalizar sugestões
 */
data class UserPreferences(
    val interests: List<String> = emptyList(),
    val previousTasks: List<String> = emptyList(),
    val context: String? = null
) 