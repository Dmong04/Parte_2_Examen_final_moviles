package com.codelab.parte_2.data.local.repositories

import com.codelab.parte_2.data.local.dao.MedidaHistoricaDao
import com.codelab.parte_2.entity.MedidaHistorica
import kotlinx.coroutines.flow.Flow

class MedidaHistoricaRepository(
    private val medidaHistoricaDao: MedidaHistoricaDao
) {
    suspend fun insertar(medida: MedidaHistorica): Long {
        return medidaHistoricaDao.insertar(medida)
    }

    suspend fun eliminar(medida: MedidaHistorica) {
        medidaHistoricaDao.eliminar(medida)
    }

    fun obtenerHistoricoPorPotrero(potreroId: Int): Flow<List<MedidaHistorica>> {
        return medidaHistoricaDao.obtenerHistoricoPorPotrero(potreroId)
    }

    suspend fun obtenerUltimaMedida(potreroId: Int): MedidaHistorica? {
        return medidaHistoricaDao.obtenerUltimaMedida(potreroId)
    }
}