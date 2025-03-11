package com.devspace.taskbeats.ui.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devspace.taskbeats.R
import com.devspace.taskbeats.data.model.Message
import com.devspace.taskbeats.data.model.OpenAiRequest
import com.devspace.taskbeats.data.remote.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApiTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Use um layout existente temporariamente

        testOpenAiApi()
    }

    private fun testOpenAiApi() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val request = OpenAiRequest(
                    model = "gpt-4o-mini",
                    messages = listOf(Message(role = "user", content = "Generate subtasks for: Read 10 pages of a book")),
                    store = true
                )

                val response = withContext(Dispatchers.IO) {
                    ApiClient.openAiService.generateSubTasks(request)
                }

                if (response.isSuccessful) {
                    val result = response.body()
                    result?.choices?.firstOrNull()?.message?.content?.let { content ->
                        Log.d("ApiTest", "Subtasks: $content")
                        Toast.makeText(this@ApiTestActivity, "Subtasks: $content", Toast.LENGTH_LONG).show()
                    } ?: run {
                        Log.d("ApiTest", "No choices in response")
                        Toast.makeText(this@ApiTestActivity, "No subtasks generated", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ApiTest", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@ApiTestActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ApiTest", "Exception: ${e.message}")
                Toast.makeText(this@ApiTestActivity, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}