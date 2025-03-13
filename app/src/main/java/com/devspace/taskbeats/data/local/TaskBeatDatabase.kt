package com.devspace.taskbeats.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class, CategoryEntity::class, SubTaskEntity::class], version = 3, exportSchema = false)
abstract class TaskBeatDatabase : RoomDatabase() {
    abstract fun getCategoryDao(): CategoryDao
    abstract fun getTaskDao(): TaskDao
}