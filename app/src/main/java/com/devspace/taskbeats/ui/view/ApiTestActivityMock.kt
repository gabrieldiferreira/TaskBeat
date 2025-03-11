package com.devspace.taskbeats.ui.view


import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devspace.taskbeats.R
import com.devspace.taskbeats.data.model.Choice
import com.devspace.taskbeats.data.model.Message
import com.devspace.taskbeats.data.model.OpenAiRequest
import com.devspace.taskbeats.data.model.OpenAiResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class ApiTestActivityMock : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

                // Simular uma resposta mockada
                val mockResponse = Response.success(
                    OpenAiResponse(
                        choices = listOf(
                            Choice(
                                message = Message(
                                    role = "Assistente",
                                    content = "1. Escolha o Livro\n2. Busque um lugar silencioso\n3. Buscar notas"
                                )
                            )
                        )
                    )
                )

                // Comentar a chamada real à API temporariamente
                // val response = withContext(Dispatchers.IO) {
                //     ApiClient.openAiService.generateSubTasks(request)
                // }

                val response = mockResponse // Usar a resposta mockada

                if (response.isSuccessful) {
                    val result = response.body()
                    result?.choices?.firstOrNull()?.message?.content?.let { content ->
                        Log.d("ApiTest", "Subtasks: $content")
                        Toast.makeText(this@ApiTestActivityMock, "Subtasks: $content", Toast.LENGTH_LONG).show()
                    } ?: run {
                        Log.d("ApiTest", "No choices in response")
                        Toast.makeText(this@ApiTestActivityMock, "No subtasks generated", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("ApiTest", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@ApiTestActivityMock, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ApiTest", "Exception: ${e.message}")
                Toast.makeText(this@ApiTestActivityMock, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}