package com.codelab.parte_2.repositories

import com.codelab.parte_2.data.local.dao.EstadoRotacionDao
import com.codelab.parte_2.data.local.repositories.EstadoRotacionRepository
import com.codelab.parte_2.entity.EstadoColor
import com.codelab.parte_2.entity.EstadoRotacion
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class EstadoRotacionRepositoryTest {

    private lateinit var estadoRotacionDao: EstadoRotacionDao
    private lateinit var repository: EstadoRotacionRepository

    @Before
    fun setUp() {
        estadoRotacionDao = mockk()
        repository = EstadoRotacionRepository(estadoRotacionDao)
    }

    @Test
    fun `insertar delega al dao y devuelve el id generado`() = runTest {
        val estado = EstadoRotacion(
            id = 0,
            potreroId = 1,
            color = EstadoColor.VERDE,
            fechaInicio = 1_700_000_000_000L
        )
        coEvery { estadoRotacionDao.insertar(estado) } returns 5L

        val resultado = repository.insertar(estado)

        assertEquals(5L, resultado)
        coVerify(exactly = 1) { estadoRotacionDao.insertar(estado) }
    }

    @Test
    fun `actualizar delega al dao`() = runTest {
        val estado = EstadoRotacion(
            id = 1,
            potreroId = 1,
            color = EstadoColor.ANARANJADO,
            fechaInicio = 1_700_000_000_000L,
            fechaFin = 1_700_100_000_000L
        )
        coEvery { estadoRotacionDao.actualizar(estado) } returns Unit

        repository.actualizar(estado)

        coVerify(exactly = 1) { estadoRotacionDao.actualizar(estado) }
    }

    @Test
    fun `eliminar delega al dao`() = runTest {
        val estado = EstadoRotacion(
            id = 1,
            potreroId = 1,
            color = EstadoColor.ROJO,
            fechaInicio = 1_700_000_000_000L
        )
        coEvery { estadoRotacionDao.eliminar(estado) } returns Unit

        repository.eliminar(estado)

        coVerify(exactly = 1) { estadoRotacionDao.eliminar(estado) }
    }

    @Test
    fun `obtenerHistoricoPorPotrero devuelve el flow del dao`() = runTest {
        val historico = listOf(
            EstadoRotacion(id = 2, potreroId = 1, color = EstadoColor.VERDE, fechaInicio = 1_700_200_000_000L),
            EstadoRotacion(id = 1, potreroId = 1, color = EstadoColor.ROJO, fechaInicio = 1_700_000_000_000L, fechaFin = 1_700_200_000_000L)
        )
        every { estadoRotacionDao.obtenerHistoricoPorPotrero(1) } returns flowOf(historico)

        val resultado = repository.obtenerHistoricoPorPotrero(1)

        var emitido: List<EstadoRotacion> = emptyList()
        resultado.collect { emitido = it }

        assertEquals(historico, emitido)
        verify(exactly = 1) { estadoRotacionDao.obtenerHistoricoPorPotrero(1) }
    }

    @Test
    fun `obtenerEstadoEnFecha devuelve el estado cuando existe`() = runTest {
        val estado = EstadoRotacion(
            id = 1,
            potreroId = 1,
            color = EstadoColor.ANARANJADO,
            fechaInicio = 1_700_000_000_000L,
            fechaFin = null
        )
        val fechaConsulta = 1_700_050_000_000L
        coEvery { estadoRotacionDao.obtenerEstadoEnFecha(1, fechaConsulta) } returns estado

        val resultado = repository.obtenerEstadoEnFecha(1, fechaConsulta)

        assertEquals(estado, resultado)
        coVerify(exactly = 1) { estadoRotacionDao.obtenerEstadoEnFecha(1, fechaConsulta) }
    }

    @Test
    fun `obtenerEstadoEnFecha devuelve null cuando no hay estado para esa fecha`() = runTest {
        val fechaConsulta = 1_600_000_000_000L
        coEvery { estadoRotacionDao.obtenerEstadoEnFecha(1, fechaConsulta) } returns null

        val resultado = repository.obtenerEstadoEnFecha(1, fechaConsulta)

        assertEquals(null, resultado)
    }

    @Test
    fun `obtenerUltimoEstado devuelve el estado mas reciente`() = runTest {
        val ultimoEstado = EstadoRotacion(
            id = 3,
            potreroId = 1,
            color = EstadoColor.VERDE,
            fechaInicio = 1_700_300_000_000L
        )
        coEvery { estadoRotacionDao.obtenerUltimoEstado(1) } returns ultimoEstado

        val resultado = repository.obtenerUltimoEstado(1)

        assertEquals(ultimoEstado, resultado)
        coVerify(exactly = 1) { estadoRotacionDao.obtenerUltimoEstado(1) }
    }

    @Test
    fun `obtenerUltimoEstado devuelve null cuando el potrero no tiene historico`() = runTest {
        coEvery { estadoRotacionDao.obtenerUltimoEstado(99) } returns null

        val resultado = repository.obtenerUltimoEstado(99)

        assertEquals(null, resultado)
    }

    @Test
    fun `obtenerTodos devuelve el flow del dao`() = runTest {
        val lista = listOf(
            EstadoRotacion(id = 1, potreroId = 1, color = EstadoColor.VERDE, fechaInicio = 1_700_000_000_000L),
            EstadoRotacion(id = 2, potreroId = 2, color = EstadoColor.ROJO, fechaInicio = 1_700_100_000_000L)
        )
        every { estadoRotacionDao.obtenerTodos() } returns flowOf(lista)

        val resultado = repository.obtenerTodos()

        var emitido: List<EstadoRotacion> = emptyList()
        resultado.collect { emitido = it }

        assertEquals(lista, emitido)
        verify(exactly = 1) { estadoRotacionDao.obtenerTodos() }
    }
}