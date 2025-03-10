package com.devspace.taskbeats.data.remote

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {

    private const val BASE_URL = "https://api.openai.com/"
    private const val API_KEY = "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"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Authorization", "Bearer $API_KEY")
                .header("Content-Type", "application/json")
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