package com.devspace.taskbeats.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_entity")
data class CategoryEntity(


    @PrimaryKey
    @ColumnInfo("key")
    val name: String,
    @ColumnInfo("is_selected")
    val isSelected: Boolean
)
