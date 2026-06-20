package com.codelab.parte_2.data.local.dao

import androidx.room.*
import com.codelab.parte_2.entity.Potrero
import kotlinx.coroutines.flow.Flow

@Dao
interface PotreroDao {

    @Insert
    suspend fun insertar(potrero: Potrero): Long

    @Update
    suspend fun actualizar(potrero: Potrero)

    @Delete
    suspend fun eliminar(potrero: Potrero)

    @Query("SELECT * FROM potrero ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<Potrero>>

    @Query("SELECT * FROM potrero WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Potrero?
}