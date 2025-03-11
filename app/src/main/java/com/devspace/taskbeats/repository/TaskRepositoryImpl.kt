package com.devspace.taskbeats.repository

import com.devspace.taskbeats.data.local.SubTaskEntity
import com.devspace.taskbeats.data.local.TaskDao
import com.devspace.taskbeats.data.local.TaskEntity
import com.devspace.taskbeats.data.model.SubTaskUiData
import com.devspace.taskbeats.data.model.TaskUiData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepositoryImpl(
    private val taskDao: TaskDao
) : TaskRepository {

    override suspend fun getTaskById(taskId: Long): TaskUiData? {
        return withContext(Dispatchers.IO) {
            taskDao.getAll().find { it.id == taskId.toLong() }?.let {
                TaskUiData(id = it.id, name = it.name, category = it.category)
            }
        }
    }

    override suspend fun getSubTasksByTaskId(taskId: Long): List<SubTaskUiData> {
        return withContext(Dispatchers.IO) {
            taskDao.getSubTasksByTaskId(taskId.toLong()).map { subTask ->
                SubTaskUiData(id = subTask.id, taskId = subTask.taskId, title = subTask.title)
            }
        }
    }

    override suspend fun generateSubTasks(taskTitle: String): List<SubTaskUiData> {
        val mockSubTasks = listOf(
            SubTaskUiData(id = 0, taskId = 0, title = "Choose the book"),
            SubTaskUiData(id = 0, taskId = 0, title = "Find a quiet place"),
            SubTaskUiData(id = 0, taskId = 0, title = "Take notes")
        )
        val taskId = withContext(Dispatchers.IO) {
            val task = TaskEntity(category = "study", name = taskTitle)
            val insertedIds = taskDao.insertAll(listOf(task))
            if (insertedIds.isNotEmpty()) insertedIds[0] else 0L
        }.toInt()
        withContext(Dispatchers.IO) {
            val subTasksToInsert = mockSubTasks.map { subTask ->
                SubTaskEntity(taskId = taskId.toLong(), title = subTask.title)
            }
            taskDao.insertSubTasks(subTasksToInsert)
        }
        return mockSubTasks.map { it.copy(taskId = taskId.toLong()) }
    }

    override suspend fun deleteTask(taskId: Long) {
        withContext(Dispatchers.IO) {
            taskDao.deleteSubTasksByTaskId(taskId.toLong())
            taskDao.deleteTask(taskId.toLong())
        }
    }
}