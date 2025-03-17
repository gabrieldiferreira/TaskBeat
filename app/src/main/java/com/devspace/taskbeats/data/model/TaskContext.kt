package com.devspace.taskbeats.data.model

data class TaskContext(
    val query: String,
    val category: String,
    val previousTasks: List<String> = emptyList()
)