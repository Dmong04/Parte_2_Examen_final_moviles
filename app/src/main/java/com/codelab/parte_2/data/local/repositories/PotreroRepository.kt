package com.codelab.parte_2.data.local.repositories

import com.codelab.parte_2.data.local.dao.PotreroDao
import com.codelab.parte_2.entity.Potrero
import kotlinx.coroutines.flow.Flow


class PotreroRepository(private val dao: PotreroDao) {
    suspend fun insertar (potrero: Potrero): Long {
        return dao.insertar(potrero);
    }

    suspend fun actualizar(potrero: Potrero) {
        dao.actualizar(potrero);
    }

    suspend fun eliminar(potrero: Potrero) {
        dao.eliminar(potrero);
    }

    fun obtenerTodos(): Flow<List<Potrero>> {
        return dao.obtenerTodos();
    }

    suspend fun obtenerPorId(id: Int): Potrero? {
        return dao.obtenerPorId(id);
    }
}