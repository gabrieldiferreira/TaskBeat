package com.devspace.taskbeats.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database([CategoryEntity::class], version =  1)
abstract class TaskBeatDatabase : RoomDatabase(){

    abstract fun getCategoryDao(): CategoryDao

    abstract fun getTaskDao(): TaskDao
}