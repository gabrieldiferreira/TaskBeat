package com.devspace.taskbeats.data.model

enum class Role{
    SYSTEM, USER, ASSISTANT
}

data class Message(
    val role: Role,
    val content: String
){
    // Converter Role para string ao serializar para JSON

    fun toApiFormat(): Map<String, String>{
        return mapOf(
            "role" to when (role) {
                Role.SYSTEM -> "System"
                Role.USER -> "user"
                Role.ASSISTANT -> "assistant"
            },
            "content" to content
        )
    }
}
