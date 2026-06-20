package com.codelab.parte_2.data.local.dao

import androidx.room.*
import com.codelab.parte_2.entity.MedidaHistorica
import kotlinx.coroutines.flow.Flow

@Dao
interface MedidaHistoricaDao {

    @Insert
    suspend fun insertar(medida: MedidaHistorica): Long

    @Delete
    suspend fun eliminar(medida: MedidaHistorica)

    @Query("SELECT * FROM medida_historica WHERE potreroId = :potreroId ORDER BY fechaRegistro DESC")
    fun obtenerHistoricoPorPotrero(potreroId: Int): Flow<List<MedidaHistorica>>

    @Query("SELECT * FROM medida_historica WHERE potreroId = :potreroId ORDER BY fechaRegistro DESC LIMIT 1")
    suspend fun obtenerUltimaMedida(potreroId: Int): MedidaHistorica?
}