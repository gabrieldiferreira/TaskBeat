package com.devspace.taskbeats.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var instance: TaskBeatDatabase? = null

    fun getDatabase(context: Context): TaskBeatDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                TaskBeatDatabase::class.java,
                "database-task-beat"
            )
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
        }
    }
}