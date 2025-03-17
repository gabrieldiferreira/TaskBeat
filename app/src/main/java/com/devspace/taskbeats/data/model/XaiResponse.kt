package com.devspace.taskbeats.data.model

/**
 * Modelo de resposta da API X.AI
 */
data class XaiResponse(
    val id: String,
    val model: String,
    val created: Long,
    val choices: List<Choice>,
    val usage: Usage
)