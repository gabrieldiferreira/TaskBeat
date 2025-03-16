package com.devspace.taskbeats.data.model.xai

import com.google.gson.annotations.SerializedName

/**
 * Modelo de resposta da API X.AI
 */
data class XaiResponse(
    val id: String,
    val model: String,
    val created: Long,
    val choices: List<XaiChoice>,
    val usage: XaiUsage
)

data class XaiChoice(
    val index: Int,
    val message: XaiMessage,
    @SerializedName("finish_reason")
    val finishReason: String
)

data class XaiUsage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

/**
 * Modelo de sugestão de tarefa
 */
data class TaskSuggestion(
    val id: String = "",
    val title: String,
    val description: String? = null,
    val category: String,
    val subtasks: List<String> = emptyList(),
    val confidenceScore: Double = 0.95
) 