package com.devspace.taskbeats.data.model

data class SubTaskUiData(
    val id: Long = 0,
    val taskId: Long,
    val name: String,
    val isCompleted: Boolean = false
)