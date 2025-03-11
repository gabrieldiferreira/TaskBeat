package com.devspace.taskbeats.ui.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.devspace.taskbeats.R
import com.devspace.taskbeats.data.local.DatabaseProvider
import com.devspace.taskbeats.repository.TaskRepository
import com.devspace.taskbeats.repository.TaskRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApiTestActivityMock : AppCompatActivity() {

    private lateinit var repository: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val db = DatabaseProvider.getDatabase(applicationContext)
        repository = TaskRepositoryImpl(db.getTaskDao())

        testOpenAiApi()
    }

    private fun testOpenAiApi() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val taskTitle = "Read 10 pages of a book"
                val subTasks = repository.generateSubTasks(taskTitle)

                Log.d("ApiTestMock", "Generated Subtasks: $subTasks")
                Toast.makeText(this@ApiTestActivityMock, "Generated Subtasks: $subTasks", Toast.LENGTH_LONG).show()

                val savedSubTasks = repository.getSubTasksByTaskId(1)
                Log.d("ApiTestMock", "Saved Subtasks from Room: $savedSubTasks")
                Toast.makeText(this@ApiTestActivityMock, "Saved Subtasks: $savedSubTasks", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("ApiTestMock", "Exception: ${e.message}")
                Toast.makeText(this@ApiTestActivityMock, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}