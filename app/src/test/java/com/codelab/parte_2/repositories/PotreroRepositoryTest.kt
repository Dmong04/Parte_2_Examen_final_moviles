package com.codelab.parte_2.repositories

import com.codelab.parte_2.data.local.dao.PotreroDao
import com.codelab.parte_2.data.local.repositories.PotreroRepository
import com.codelab.parte_2.entity.Potrero
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

class PotreroRepositoryTest {

    private lateinit var potreroDao: PotreroDao
    private lateinit var repository: PotreroRepository

    @Before
    fun setUp() {
        potreroDao = mockk()
        repository = PotreroRepository(potreroDao)
    }

    @Test
    fun `insertar delega al dao y devuelve el id generado`(): Unit = runTest {
        val potrero = Potrero(
            id = 0,
            nombre = "Potrero Norte",
            medidaM2 = 2500.0,
            fechaCreacion = 1_700_000_000_000L
        )
        coEvery { potreroDao.insertar(potrero) } returns 7L

        val resultado = repository.insertar(potrero)

        assertEquals(7L, resultado)
        coVerify(exactly = 1) { potreroDao.insertar(potrero) }
    }

    @Test
    fun `actualizar delega al dao`(): Unit = runTest {
        val potrero = Potrero(
            id = 1,
            nombre = "Potrero Sur",
            medidaM2 = 3000.0,
            fechaCreacion = 1_700_000_000_000L
        )
        coEvery { potreroDao.actualizar(potrero) } returns Unit

        repository.actualizar(potrero)

        coVerify(exactly = 1) { potreroDao.actualizar(potrero) }
    }

    @Test
    fun `eliminar delega al dao`(): Unit = runTest {
        val potrero = Potrero(
            id = 1,
            nombre = "Potrero Sur",
            medidaM2 = 3000.0,
            fechaCreacion = 1_700_000_000_000L
        )
        coEvery { potreroDao.eliminar(potrero) } returns Unit

        repository.eliminar(potrero)

        coVerify(exactly = 1) { potreroDao.eliminar(potrero) }
    }

    @Test
    fun `obtenerPorId devuelve el potrero cuando existe`(): Unit = runTest {
        val potrero = Potrero(
            id = 1,
            nombre = "Potrero Sur",
            medidaM2 = 3000.0,
            fechaCreacion = 1_700_000_000_000L
        )
        coEvery { potreroDao.obtenerPorId(1) } returns potrero

        val resultado = repository.obtenerPorId(1)

        assertEquals(potrero, resultado)
    }

    @Test
    fun `obtenerPorId devuelve null cuando no existe`(): Unit = runTest {
        coEvery { potreroDao.obtenerPorId(99) } returns null

        val resultado = repository.obtenerPorId(99)

        assertEquals(null, resultado)
    }

    @Test
    fun `obtenerTodos devuelve el flow del dao`(): Unit = runTest {
        val lista = listOf(
            Potrero(id = 1, nombre = "A", medidaM2 = 1000.0, fechaCreacion = 1_700_000_000_000L),
            Potrero(id = 2, nombre = "B", medidaM2 = 2000.0, fechaCreacion = 1_700_000_100_000L)
        )
        every { potreroDao.obtenerTodos() } returns flowOf(lista)

        val resultado = repository.obtenerTodos()

        var emitido: List<Potrero> = emptyList()
        resultado.collect { emitido = it }

        assertEquals(lista, emitido)
        verify(exactly = 1) { potreroDao.obtenerTodos() }
    }
}