package com.devspace.taskbeats.data.remote

import android.util.Log
import com.devspace.taskbeats.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {

    private const val OPENAI_BASE_URL = "https://api.openai.com/v1/"
    private const val XAI_BASE_URL = "https://api.x.ai/v1/" // API X.AI oficial

    private val xaiOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            // Usar a API key fornecida diretamente para testes
            val apiKey = BuildConfig.XAI_API_KEY.trim()
            Log.d("ApiClient", "X.AI API Key configurada")
            
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    private val openAiRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(OPENAI_BASE_URL)
        .client(xaiOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()

    private val xaiRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(XAI_BASE_URL)
        .client(xaiOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()

    val openAiService: XaiService by lazy {
        openAiRetrofit.create(XaiService::class.java)
    }

    val xaiService: XaiService by lazy {
        xaiRetrofit.create(XaiService::class.java)
    }
}