package com.devspace.taskbeats.data.model

import com.google.gson.annotations.SerializedName

data class Choice(
    val index: Int,
    val message: Message,
    @SerializedName("finish_reason")
    val finishReason: String
)
