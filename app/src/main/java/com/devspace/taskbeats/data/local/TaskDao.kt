package com.devspace.taskbeats.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubTaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Update
    suspend fun updateSubtask(subtask: SubTaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Delete
    suspend fun deleteSubtask(subtask: SubTaskEntity)

    @Query("SELECT * FROM task_entity")
    fun getAllTasks(): LiveData<List<TaskEntity>>

    @Transaction
    @Query("SELECT * FROM task_entity")
    fun getTasksWithSubtasks(): LiveData<List<TaskWithSubtasks>>

    @Transaction
    @Query("SELECT * FROM task_entity WHERE categoryId = :categoryId")
    fun getTasksByCategory(categoryId: Long): LiveData<List<TaskEntity>>

    @Transaction
    @Query("SELECT * FROM task_entity WHERE id = :taskId")
    suspend fun getTaskWithSubtasksById(taskId: Long): TaskWithSubtasks?

    @Query("SELECT * FROM task_entity WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Query("SELECT * FROM sub_task_entity WHERE taskId = :taskId")
    fun getSubtasksForTask(taskId: Long): LiveData<List<SubTaskEntity>>

    /**
     * Obtém as tarefas mais recentes, limitadas pelo parâmetro limit
     */
    @Query("SELECT * FROM task_entity ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentTasks(limit: Int): List<TaskEntity>
}