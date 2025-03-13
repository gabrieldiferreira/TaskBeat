package com.devspace.taskbeats.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

/**
 * Entidade que representa uma tarefa no banco de dados.
 * As tarefas são associadas a uma categoria e podem ter subtarefas.
 */
@Entity(
    tableName = "task_entity",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TaskEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val name: String,
    val isCompleted: Boolean = false
)