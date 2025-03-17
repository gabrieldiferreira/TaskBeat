package com.devspace.taskbeats.data.model

data class TaskSuggestion(
    val id: String = "",
    val title: String,
    val description: String? = null,
    val category: String,
    val subtasks: List<String> = emptyList(),
    val confidenceScore: Double = 0.95
)