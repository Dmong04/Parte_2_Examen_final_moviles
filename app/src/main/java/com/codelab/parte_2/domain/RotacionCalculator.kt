package com.codelab.parte_2.domain

import com.codelab.parte_2.entity.EstadoColor
import com.codelab.parte_2.entity.EstadoRotacion
import java.util.concurrent.TimeUnit

object RotacionCalculator {

    const val DIAS_ROJO = 5
    const val DIAS_ANARANJADO = 15

    private val MS_POR_DIA = TimeUnit.DAYS.toMillis(1)

    fun calcularColorEnFecha(estado: EstadoRotacion, fechaConsulta: Long): EstadoColor {
        if (fechaConsulta < estado.fechaInicio) return EstadoColor.VERDE

        return when (estado.color) {
            EstadoColor.VERDE -> EstadoColor.VERDE

            EstadoColor.ROJO -> {
                val diasTranscurridos = diasEntre(estado.fechaInicio, fechaConsulta)
                when {
                    diasTranscurridos < DIAS_ROJO -> EstadoColor.ROJO
                    diasTranscurridos < DIAS_ROJO + DIAS_ANARANJADO -> EstadoColor.ANARANJADO
                    else -> EstadoColor.VERDE
                }
            }

            EstadoColor.ANARANJADO -> {
                val diasTranscurridos = diasEntre(estado.fechaInicio, fechaConsulta)
                when {
                    diasTranscurridos < DIAS_ANARANJADO -> EstadoColor.ANARANJADO
                    else -> EstadoColor.VERDE
                }
            }
        }
    }

    fun fechaFinRojo(fechaInicioRojo: Long): Long =
        fechaInicioRojo + DIAS_ROJO * MS_POR_DIA

    fun fechaFinAnaranjado(fechaInicioRojo: Long): Long =
        fechaFinRojo(fechaInicioRojo) + DIAS_ANARANJADO * MS_POR_DIA

    private fun diasEntre(inicio: Long, fin: Long): Long =
        (fin - inicio) / MS_POR_DIA
}