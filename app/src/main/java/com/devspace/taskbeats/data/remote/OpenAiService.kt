package com.devspace.taskbeats.data.remote

import com.devspace.taskbeats.data.model.OpenAiRequest
import com.devspace.taskbeats.data.model.OpenAiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAiService {
    @POST("v1/chat/completions")
    suspend fun generateSubTasks(@Body request: OpenAiRequest): Response<OpenAiResponse>
}