package com.devspace.taskbeats.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface CategoryDao {
    /**
     * Retorna um LiveData com todas as categorias armazenadas no banco de dados.
     * A UI será notificada automaticamente de qualquer mudança nos dados.
     */
    @Query("SELECT * FROM category_entity")
    fun getAll(): LiveData<List<CategoryEntity>>

    /**
     * Busca uma categoria específica pelo seu ID.
     * @param id O identificador único da categoria.
     * @return A categoria correspondente, ou null se não existir.
     */
    @Query("SELECT * FROM category_entity WHERE id = :id")
    suspend fun getById(id: kotlin.Long): CategoryEntity?

    /**
     * Busca uma categoria específica pelo seu nome.
     * @param name O nome da categoria.
     * @return A categoria correspondente, ou null se não existir.
     */
    @Query("SELECT * FROM category_entity WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): CategoryEntity?

    /**
     * Insere ou substitui uma lista de categorias no banco de dados.
     * @param categoryEntities Lista de categorias a serem inseridas.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categoryEntities: List<CategoryEntity>)

    /**
     * Insere ou substitui uma única categoria no banco de dados.
     * @param categoryEntity A categoria a ser inserida.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(categoryEntity: CategoryEntity) : Long

    /**
     * Atualiza uma categoria existente no banco de dados.
     * @param categoryEntity A categoria a ser atualizada.
     */
    @Update
    suspend fun update(categoryEntity: CategoryEntity)

    /**
     * Exclui uma categoria do banco de dados.
     * @param categoryEntity A categoria a ser excluída.
     */
    @Delete
    suspend fun delete(categoryEntity: CategoryEntity)

    /**
     * Exclui todas as categorias do banco de dados.
     */
    @Query("DELETE FROM category_entity")
    suspend fun deleteAll()
}