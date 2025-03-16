package com.devspace.taskbeats.data.remote

import com.devspace.taskbeats.data.model.xai.XaiRequest
import com.devspace.taskbeats.data.model.xai.XaiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interface de serviço para a API X.AI
 */
interface XaiService {
    @POST("chat/completions")
    suspend fun getSuggestions(@Body request: XaiRequest): Response<XaiResponse>
} 