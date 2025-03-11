package com.devspace.taskbeats.repository

import com.devspace.taskbeats.data.local.TaskDao
import com.devspace.taskbeats.data.local.TaskEntity
import com.devspace.taskbeats.data.model.SubTaskUiData
import com.devspace.taskbeats.data.model.TaskUiData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepositoryImpl(
    private val taskDao: TaskDao
) : TaskRepository {

    override suspend fun getTaskById(taskId: Int): TaskUiData? {
        return withContext(Dispatchers.IO) {
            taskDao.getAll().find { it.id == taskId.toLong() }?.let {
                TaskUiData(id = it.id.toString(), name = it.name, category = it.category)
            }
        }
    }

    override suspend fun getSubTasksByTaskId(taskId: Int): List<SubTaskUiData> {
        // Placeholder: SubTaskEntity ainda não está implementado. Adicione quando disponível.
        return emptyList() // Substitua por lógica real com SubTaskDao
    }

    override suspend fun generateSubTasks(taskTitle: String): List<SubTaskUiData> {
        val mockSubTasks = listOf(
            SubTaskUiData(id = 1, taskId = 0, title = "Choose the book"),
            SubTaskUiData(id = 2, taskId = 0, title = "Find a quiet place"),
            SubTaskUiData(id = 3, taskId = 0, title = "Take notes")
        )
        withContext(Dispatchers.IO) {
            val task = TaskEntity(category = "study", name = taskTitle)
            val taskId = taskDao.insertAll(listOf(task))[0].toInt()
            // Placeholder para SubTaskEntity
        }
        return mockSubTasks
    }

    override suspend fun deleteTask(taskId: Int) {
        withContext(Dispatchers.IO) {
            taskDao.getAll().find { it.id == taskId.toLong() }?.let { task ->
                taskDao.insertAll(listOf(task)) // Placeholder, substitua por delete real
            }
        }
    }
}