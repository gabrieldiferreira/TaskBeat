package com.devspace.taskbeats.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de requisição para a API X.AI
 */
data class XaiRequest(
    val messages: List<com.devspace.taskbeats.data.model.Message>,
    val model: String = "grok-2-latest",
    val stream: Boolean = false,
    val temperature: Double = 0.7,
    
    // Campos adicionais para metadados
    @SerializedName("task_context")
    val taskContext: TaskContext? = null
)

