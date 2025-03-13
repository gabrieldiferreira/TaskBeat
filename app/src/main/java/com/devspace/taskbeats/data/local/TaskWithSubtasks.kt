package com.devspace.taskbeats.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubTaskEntity>
)
