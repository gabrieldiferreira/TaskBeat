package com.devspace.taskbeats.data.local

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var INSTANCE: TaskBeatDatabase? = null

    fun getDatabase(context: Context): TaskBeatDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                TaskBeatDatabase::class.java,
                "taskbeat_db"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}