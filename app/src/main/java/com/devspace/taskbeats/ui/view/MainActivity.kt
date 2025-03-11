package com.devspace.taskbeats.ui.view

import TaskListAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.devspace.taskbeats.R
import com.devspace.taskbeats.data.local.CategoryEntity
import com.devspace.taskbeats.data.local.DatabaseProvider
import com.devspace.taskbeats.data.local.TaskEntity
import com.devspace.taskbeats.data.model.CategoryUiData
import com.devspace.taskbeats.data.model.TaskUiData
import com.devspace.taskbeats.repository.TaskRepository
import com.devspace.taskbeats.repository.TaskRepositoryImpl
import com.devspace.taskbeats.ui.adapter.CategoryListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val db by lazy { DatabaseProvider.getDatabase(applicationContext) }
    private val repository: TaskRepository by lazy { TaskRepositoryImpl(db.getTaskDao()) }
    private val categoryDao by lazy { db.getCategoryDao() }
    private val taskDao by lazy { db.getTaskDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        insertDefaultCategory()
        insertDefaultTasks()

        val rvCategory = findViewById<RecyclerView>(R.id.rv_categories)
        val rvTask = findViewById<RecyclerView>(R.id.rv_tasks)

        val taskAdapter = TaskListAdapter()
        val categoryAdapter = CategoryListAdapter()

        categoryAdapter.setOnClickListener { selected ->
            CoroutineScope(Dispatchers.Main).launch {
                val categoriesFromDb = withContext(Dispatchers.IO) { categoryDao.getAll() }
                val categoryTemp = categoriesFromDb.map { item ->
                    when {
                        item.name == selected.name && !item.isSelected -> item.copy(isSelected = true)
                        item.name == selected.name && item.isSelected -> item.copy(isSelected = false)
                        else -> item
                    }
                }
                withContext(Dispatchers.IO) { categoryDao.insertAll(categoryTemp) }

                val categoryUiTemp = categoryTemp.map { CategoryUiData(name = it.name, isSelected = it.isSelected) }
                val taskTemp = if (selected.name != "ALL") {
                    withContext(Dispatchers.IO) { taskDao.getAll() }
                        .filter { it.category == selected.name }
                        .map { TaskUiData(id = it.id, name = it.name, category = it.category) }
                } else {
                    withContext(Dispatchers.IO) { taskDao.getAll() }
                        .map { TaskUiData(id = it.id, name = it.name, category = it.category) }
                }
                taskAdapter.submitList(taskTemp)
                categoryAdapter.submitList(categoryUiTemp)
            }
        }

        rvCategory.adapter = categoryAdapter
        getCategoriesFromDataBase(categoryAdapter)

        rvTask.adapter = taskAdapter
        getTasksFromDatabase(taskAdapter)
    }

    private fun insertDefaultCategory() {
        val categoriesEntity = listOf(
            CategoryUiData(name = "ALL", isSelected = true),
            CategoryUiData(name = "STUDY", isSelected = false),
            CategoryUiData(name = "HEALTH", isSelected = false),
            CategoryUiData(name = "WELLNESS", isSelected = false),
            CategoryUiData(name = "HOME", isSelected = false),
            CategoryUiData(name = "WORK", isSelected = false)
        ).map {
            CategoryEntity(name = it.name, isSelected = it.isSelected)
        }
        CoroutineScope(Dispatchers.IO).launch {
            categoryDao.insertAll(categoriesEntity)
        }
    }

    private fun insertDefaultTasks() {
        val tasksEntities = listOf(
            TaskUiData(id = 0, name = "Ler 10 páginas do livro atual", category = "STUDY"),
            TaskUiData(id = 0, name = "45 min de treino na academia", category = "HEALTH"),
            TaskUiData(id = 0, name = "Correr 5km", category = "HEALTH"),
            TaskUiData(id = 0, name = "Meditar por 10 min", category = "WELLNESS"),
            TaskUiData(id = 0, name = "Silêncio total por 5 min", category = "WELLNESS"),
            TaskUiData(id = 0, name = "Descer o lixo", category = "HOME"),
            TaskUiData(id = 0, name = "Tirar caixas da garagem", category = "HOME"),
            TaskUiData(id = 0, name = "Lavar o carro", category = "HOME"),
            TaskUiData(id = 0, name = "Gravar aulas DevSpace", category = "WORK"),
            TaskUiData(id = 0, name = "Criar planejamento de vídeos da semana", category = "WORK"),
            TaskUiData(id = 0, name = "Soltar reels da semana", category = "WORK")
        ).map {
            TaskEntity(name = it.name, category = it.category)
        }
        CoroutineScope(Dispatchers.IO).launch {
            taskDao.insertAll(tasksEntities)
        }
    }

    private fun getCategoriesFromDataBase(adapter: CategoryListAdapter) {
        CoroutineScope(Dispatchers.IO).launch {
            val categoriesFromDb = categoryDao.getAll()
            val categoriesUiData = categoriesFromDb.map {
                CategoryUiData(name = it.name, isSelected = it.isSelected)
            }
            withContext(Dispatchers.Main) {
                adapter.submitList(categoriesUiData)
            }
        }
    }

    private fun getTasksFromDatabase(adapter: TaskListAdapter) {
        CoroutineScope(Dispatchers.IO).launch {
            val tasksFromDb = taskDao.getAll()
            val tasksUiData = tasksFromDb.map {
                TaskUiData(id = it.id, name = it.name, category = it.category)
            }
            withContext(Dispatchers.Main) {
                adapter.submitList(tasksUiData)
            }
        }
    }
}