package com.codelab.parte_2.data.local.repositories

import com.codelab.parte_2.data.local.dao.EstadoRotacionDao
import com.codelab.parte_2.entity.EstadoRotacion
import kotlinx.coroutines.flow.Flow

class EstadoRotacionRepository(
    private val estadoRotacionDao: EstadoRotacionDao
) {
    suspend fun insertar(estado: EstadoRotacion): Long {
        return estadoRotacionDao.insertar(estado)
    }

    suspend fun actualizar(estado: EstadoRotacion) {
        estadoRotacionDao.actualizar(estado)
    }

    suspend fun eliminar(estado: EstadoRotacion) {
        estadoRotacionDao.eliminar(estado)
    }

    fun obtenerHistoricoPorPotrero(potreroId: Int): Flow<List<EstadoRotacion>> {
        return estadoRotacionDao.obtenerHistoricoPorPotrero(potreroId)
    }

    suspend fun obtenerEstadoEnFecha(potreroId: Int, fecha: Long): EstadoRotacion? {
        return estadoRotacionDao.obtenerEstadoEnFecha(potreroId, fecha)
    }

    suspend fun obtenerUltimoEstado(potreroId: Int): EstadoRotacion? {
        return estadoRotacionDao.obtenerUltimoEstado(potreroId)
    }

    fun obtenerTodos(): Flow<List<EstadoRotacion>> {
        return estadoRotacionDao.obtenerTodos()
    }
}