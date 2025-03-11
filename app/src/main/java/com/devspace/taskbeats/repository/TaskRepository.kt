package com.devspace.taskbeats.repository

import com.devspace.taskbeats.data.model.SubTaskUiData
import com.devspace.taskbeats.data.model.TaskUiData

interface TaskRepository {
    suspend fun getTaskById(taskId: Long): TaskUiData?
    suspend fun getSubTasksByTaskId(taskId: Long): List<SubTaskUiData>
    suspend fun generateSubTasks(taskTitle: String): List<SubTaskUiData>
    suspend fun deleteTask(taskId: Long)
}