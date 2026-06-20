package com.codelab.parte_2.repositories

import com.codelab.parte_2.data.local.dao.MedidaHistoricaDao
import com.codelab.parte_2.data.local.repositories.MedidaHistoricaRepository
import com.codelab.parte_2.entity.MedidaHistorica
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

class MedidaHistoricaRepositoryTest {

    private lateinit var medidaHistoricaDao: MedidaHistoricaDao
    private lateinit var repository: MedidaHistoricaRepository

    @Before
    fun setUp() {
        medidaHistoricaDao = mockk()
        repository = MedidaHistoricaRepository(medidaHistoricaDao)
    }

    @Test
    fun `insertar delega al dao y devuelve el id generado`() = runTest {
        val medida = MedidaHistorica(
            id = 0,
            potreroId = 1,
            medidaM2 = 2500.0,
            fechaRegistro = 1_700_000_000_000L
        )
        coEvery { medidaHistoricaDao.insertar(medida) } returns 10L

        val resultado = repository.insertar(medida)

        assertEquals(10L, resultado)
        coVerify(exactly = 1) { medidaHistoricaDao.insertar(medida) }
    }

    @Test
    fun `eliminar delega al dao`() = runTest {
        val medida = MedidaHistorica(
            id = 1,
            potreroId = 1,
            medidaM2 = 2500.0,
            fechaRegistro = 1_700_000_000_000L
        )
        coEvery { medidaHistoricaDao.eliminar(medida) } returns Unit

        repository.eliminar(medida)

        coVerify(exactly = 1) { medidaHistoricaDao.eliminar(medida) }
    }

    @Test
    fun `obtenerHistoricoPorPotrero devuelve el flow del dao`() = runTest {
        val historico = listOf(
            MedidaHistorica(id = 2, potreroId = 1, medidaM2 = 2600.0, fechaRegistro = 1_700_200_000_000L),
            MedidaHistorica(id = 1, potreroId = 1, medidaM2 = 2500.0, fechaRegistro = 1_700_000_000_000L)
        )
        every { medidaHistoricaDao.obtenerHistoricoPorPotrero(1) } returns flowOf(historico)

        val resultado = repository.obtenerHistoricoPorPotrero(1)

        var emitido: List<MedidaHistorica> = emptyList()
        resultado.collect { emitido = it }

        assertEquals(historico, emitido)
        verify(exactly = 1) { medidaHistoricaDao.obtenerHistoricoPorPotrero(1) }
    }

    @Test
    fun `obtenerUltimaMedida devuelve la medida mas reciente cuando existe`() = runTest {
        val ultimaMedida = MedidaHistorica(
            id = 3,
            potreroId = 1,
            medidaM2 = 2700.0,
            fechaRegistro = 1_700_300_000_000L
        )
        coEvery { medidaHistoricaDao.obtenerUltimaMedida(1) } returns ultimaMedida

        val resultado = repository.obtenerUltimaMedida(1)

        assertEquals(ultimaMedida, resultado)
        coVerify(exactly = 1) { medidaHistoricaDao.obtenerUltimaMedida(1) }
    }

    @Test
    fun `obtenerUltimaMedida devuelve null cuando el potrero no tiene mediciones`() = runTest {
        coEvery { medidaHistoricaDao.obtenerUltimaMedida(99) } returns null

        val resultado = repository.obtenerUltimaMedida(99)

        assertEquals(null, resultado)
    }
}