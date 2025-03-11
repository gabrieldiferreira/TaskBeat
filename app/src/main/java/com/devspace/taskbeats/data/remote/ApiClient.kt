package com.devspace.taskbeats.data.remote

import android.util.Log
import com.devspace.taskbeats.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {

    private const val BASE_URL = "https://api.openai.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val apiKey = BuildConfig.OPENAI_API_KEY
            Log.d("ApiClient", "API Key: $apiKey")
            if (apiKey.isEmpty()) {
                Log.e("ApiClient", "API Key is empty or not loaded!")
            }
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()


    val openAiService: OpenAiService by lazy {
        retrofit.create(OpenAiService::class.java)

    }
}