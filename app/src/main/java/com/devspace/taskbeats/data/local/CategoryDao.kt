package com.devspace.taskbeats.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categoryentity")
    fun getALL(): List<CategoryEntity>

    @Insert
    fun insertAll(categoryEntity: List<CategoryEntity>)
}