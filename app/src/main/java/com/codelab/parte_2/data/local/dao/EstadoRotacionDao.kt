package com.codelab.parte_2.data.local.dao

import androidx.room.*
import com.codelab.parte_2.entity.EstadoRotacion
import kotlinx.coroutines.flow.Flow

@Dao
interface EstadoRotacionDao {

    @Insert
    suspend fun insertar(estado: EstadoRotacion): Long

    @Update
    suspend fun actualizar(estado: EstadoRotacion)

    @Delete
    suspend fun eliminar(estado: EstadoRotacion)

    @Query("SELECT * FROM estado_rotacion WHERE potreroId = :potreroId ORDER BY fechaInicio DESC")
    fun obtenerHistoricoPorPotrero(potreroId: Int): Flow<List<EstadoRotacion>>

    @Query("""
        SELECT * FROM estado_rotacion 
        WHERE potreroId = :potreroId 
        AND fechaInicio <= :fecha 
        AND (fechaFin IS NULL OR fechaFin >= :fecha)
        ORDER BY fechaInicio DESC LIMIT 1
    """)
    suspend fun obtenerEstadoEnFecha(potreroId: Int, fecha: Long): EstadoRotacion?

    @Query("SELECT * FROM estado_rotacion WHERE potreroId = :potreroId ORDER BY fechaInicio DESC LIMIT 1")
    suspend fun obtenerUltimoEstado(potreroId: Int): EstadoRotacion?

    @Query("SELECT * FROM estado_rotacion ORDER BY fechaInicio DESC")
    fun obtenerTodos(): Flow<List<EstadoRotacion>>
}