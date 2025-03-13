package com.devspace.taskbeats.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade que representa uma subtarefa no banco de dados.
 * As subtarefas são associadas a uma tarefa principal.
 */
@Entity(
    tableName = "sub_task_entity",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["taskId"])]
)
data class SubTaskEntity(
    /**
     * Identificador único da subtarefa, gerado automaticamente.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Identificador da tarefa à qual a subtarefa pertence.
     */
    val taskId: Long,

    /**
     * Título da subtarefa.
     */
    val title: String,

    /**
     * Indica se a subtarefa foi concluída.
     */
    val isCompleted: Boolean = false
)