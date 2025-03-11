package com.devspace.taskbeats.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TaskDao {

    @Query("SELECT * FROM task_entity")
    fun getAll(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(taskEntities: List<TaskEntity>) : List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubTasks(subTasks: List<SubTaskEntity>): List<Long>

    @Query("SELECT * FROM sub_task_entity WHERE taskId = :taskId")
    fun getSubTasksByTaskId(taskId: Long): List<SubTaskEntity>

    @Query("DELETE FROM sub_task_entity WHERE taskId = :taskId")
    fun deleteSubTasksByTaskId(taskId: Long)

    @Query("DELETE FROM task_entity WHERE id = :taskId")
    fun deleteTask(taskId: Long)
}