package com.devspace.taskbeats.data.model

data class OpenAiRequest(

    val model: String,
    val messages: List<Message>,
    val store: Boolean = false
)
