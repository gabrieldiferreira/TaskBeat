package com.devspace.taskbeats.ui.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.devspace.taskbeats.R
import com.devspace.taskbeats.data.local.CategoryEntity
import com.devspace.taskbeats.data.local.TaskBeatDatabase
import com.devspace.taskbeats.data.local.TaskEntity
import com.devspace.taskbeats.data.model.CategoryUiData
import com.devspace.taskbeats.data.model.TaskUiData
import com.devspace.taskbeats.ui.adapter.CategoryListAdapter
import com.devspace.taskbeats.ui.adapter.TaskListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            TaskBeatDatabase::class.java, "database-task-beat"
        ).build()
    }

    private val categoryDao by lazy {
        db.getCategoryDao()
    }

    private val taskDao by lazy {
        db.getTaskDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        insertDefaultCategory()

        val rvCategory = findViewById<RecyclerView>(R.id.rv_categories)
        val rvTask = findViewById<RecyclerView>(R.id.rv_tasks)

        val taskAdapter = TaskListAdapter()
        val categoryAdapter = CategoryListAdapter()

        categoryAdapter.setOnClickListener { selected ->
            val categoryTemp = categories.map { item ->
                when {
                    item.name == selected.name && !item.isSelected -> item.copy(isSelected = true)
                    item.name == selected.name && item.isSelected -> item.copy(isSelected = false)
                    else -> item
                }
            }

            val taskTemp =
                if (selected.name != "ALL") {
                    tasks.filter { it.category == selected.name }
                } else {
                    tasks
                }
            taskAdapter.submitList(taskTemp)

            categoryAdapter.submitList(categoryTemp)
        }

        rvCategory.adapter = categoryAdapter
        getCategoriesFromDataBase(categoryAdapter)

        rvTask.adapter = taskAdapter
        taskAdapter.submitList(tasks)
    }
    private fun insertDefaultCategory(){
        val categoriesEntity = categories.map {
            CategoryEntity(
                name = it.name,
                isSelected = it.isSelected
            )
        }
        GlobalScope.launch(Dispatchers.IO) {
            categoryDao.insertAll(categoriesEntity)
        }
    }

    private fun insertDefaultTasks() {
        val tasksEntities = tasks.map {
            TaskEntity(
                name = it.name,
                category = it.category
            )
        }
    }



    private fun getCategoriesFromDataBase(adapter: CategoryListAdapter) {
        GlobalScope.launch (Dispatchers.IO) {
            val categoriesFromDb: List<CategoryEntity> = categoryDao.getAll()
            val categoriesUiData = categoriesFromDb.map {
                CategoryUiData(
                    name = it.name,
                    isSelected = it.isSelected
                )
            }
            adapter.submitList(categoriesUiData)
        }
    }
}

val categories: List<CategoryUiData> = listOf()

val tasks = listOf(
    TaskUiData(
        "Ler 10 páginas do livro atual",
        "STUDY",
        "e"
    ),
    TaskUiData(
        "45 min de treino na academia",
        "HEALTH",
        "e"
    ),
    TaskUiData(
        "Correr 5km",
        "HEALTH","e"
    ),
    TaskUiData(
        "Meditar por 10 min",
        "WELLNESS","e"
    ),
    TaskUiData(
        "Silêncio total por 5 min",
        "WELLNESS",
        "e"
    ),
    TaskUiData(
        "Descer o livo",
        "HOME",""
    ),
    TaskUiData(
        "Tirar caixas da garagem",
        "HOME",
        "e"
    ),
    TaskUiData(
        "Lavar o carro",
        "HOME",
        "e"
    ),
    TaskUiData(
        "Gravar aulas DevSpace",
        "WORK",
        "g"
    ),
    TaskUiData(
        "Criar planejamento de vídeos da semana",
        "WORK",
        "e"
    ),
    TaskUiData(
        "Soltar reels da semana",
        "WORK",
        "r"
    ),
)