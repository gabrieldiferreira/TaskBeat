package com.devspace.taskbeats.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "category_entity", indices = [Index(value = ["name"], unique = true)])
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isSelected: Boolean = false
)