package com.codelab.parte_2.ui.rotacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelab.parte_2.data.local.repositories.EstadoRotacionRepository
import com.codelab.parte_2.data.local.repositories.PotreroRepository
import com.codelab.parte_2.domain.RotacionCalculator
import com.codelab.parte_2.entity.EstadoColor
import com.codelab.parte_2.entity.EstadoRotacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RotacionViewModel(
    private val potreroRepository: PotreroRepository,
    private val estadoRotacionRepository: EstadoRotacionRepository
) : ViewModel() {

    // Fecha que el usuario tiene seleccionada en el MaterialDatePicker.
    // Por defecto, hoy (requisito 3.b).
    private val _fechaSeleccionada = MutableStateFlow(hoyNormalizado())
    val fechaSeleccionada: StateFlow<Long> = _fechaSeleccionada.asStateFlow()

    // IDs de potreros marcados (checkbox) para la acción "cargar con ganado"
    private val _seleccionIds = MutableStateFlow<Set<Int>>(emptySet())

    /**
     * Lista combinada: por cada potrero, su color calculado en la fecha
     * seleccionada + si está marcado. Se recalcula solo cuando cambia la
     * fecha, la lista de potreros, o el set de estados en Room.
     */
    val potrerosConEstado: StateFlow<List<PotreroConEstado>> = combine(
        potreroRepository.obtenerTodos(),
        estadoRotacionRepository.obtenerTodos(),
        _fechaSeleccionada,
        _seleccionIds
    ) { potreros, estados, fecha, seleccionIds ->
        potreros.map { potrero ->
            val estadosDelPotrero = estados.filter { it.potreroId == potrero.id }
            val ultimoEstadoVigente = estadoVigenteEnFecha(estadosDelPotrero, fecha)
            val color = ultimoEstadoVigente?.let {
                RotacionCalculator.calcularColorEnFecha(it, fecha)
            } ?: EstadoColor.VERDE

            PotreroConEstado(
                potrero = potrero,
                colorActual = color,
                seleccionado = seleccionIds.contains(potrero.id)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    /**
     * De todos los EstadoRotacion de un potrero, busca el que corresponde
     * a la fecha de consulta: el más reciente cuyo fechaInicio sea <= fecha.
     * (No usamos fechaFin acá porque ese campo solo se "cierra" cuando el
     * usuario carga ganado de nuevo; el cálculo de vencimiento por días lo
     * hace RotacionCalculator, no esta búsqueda).
     */
    private fun estadoVigenteEnFecha(estados: List<EstadoRotacion>, fecha: Long): EstadoRotacion? {
        return estados
            .filter { it.fechaInicio <= fecha }
            .maxWithOrNull(compareBy({ it.fechaInicio }, { it.id }))
    }

    fun seleccionarFecha(fechaMillis: Long) {
        _fechaSeleccionada.value = fechaMillis
    }

    /**
     * Marca/desmarca un potrero para la acción "cargar con ganado". Solo
     * tiene efecto si el potrero está actualmente en VERDE; esto es una
     * defensa adicional, ya que el Adapter no debería disparar este
     * llamado para potreros en ROJO/ANARANJADO en primer lugar.
     */
    fun toggleSeleccion(potreroId: Int) {
        val colorActual = potrerosConEstado.value
            .firstOrNull { it.potrero.id == potreroId }
            ?.colorActual

        if (colorActual != EstadoColor.VERDE) return

        val actual = _seleccionIds.value
        _seleccionIds.value = if (actual.contains(potreroId)) {
            actual - potreroId
        } else {
            actual + potreroId
        }
    }

    fun limpiarSeleccion() {
        _seleccionIds.value = emptySet()
    }

    /**
     * Acción del botón "Cargar con ganado": para cada potrero seleccionado,
     * cierra su estado VERDE vigente (le pone fechaFin) e inserta un nuevo
     * EstadoRotacion en ROJO empezando en la fecha seleccionada del picker.
     *
     * Solo se puede llegar a seleccionar potreros en VERDE: el Adapter
     * bloquea el checkbox de los que están en ROJO/ANARANJADO, así que no
     * hace falta volver a validar el color acá.
     */
    fun cargarGanado(onResult: (exito: Boolean) -> Unit) {
        val idsSeleccionados = _seleccionIds.value
        if (idsSeleccionados.isEmpty()) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                val fecha = _fechaSeleccionada.value

                for (potreroId in idsSeleccionados) {
                    val ultimoEstado = estadoRotacionRepository.obtenerUltimoEstado(potreroId)

                    // Cierra el estado verde vigente (si existía y seguía abierto)
                    if (ultimoEstado != null && ultimoEstado.fechaFin == null) {
                        estadoRotacionRepository.actualizar(
                            ultimoEstado.copy(fechaFin = fecha)
                        )
                    }

                    // Abre el nuevo estado rojo
                    estadoRotacionRepository.insertar(
                        EstadoRotacion(
                            potreroId = potreroId,
                            color = EstadoColor.ROJO,
                            fechaInicio = fecha,
                            fechaFin = RotacionCalculator.fechaFinRojo(fecha)
                        )
                    )
                }

                limpiarSeleccion()
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun obtenerHistorico(potreroId: Int) = estadoRotacionRepository.obtenerHistoricoPorPotrero(potreroId)

    /**
     * "Hoy" a medianoche, en UTC. Usamos UTC (no la zona local) porque
     * MaterialDatePicker trabaja siempre en UTC internamente; si mezclamos
     * un "hoy" en hora local con fechas que vienen del picker en UTC, el
     * cálculo de días transcurridos en RotacionCalculator puede desfasarse
     * y saltarse un día (sobre todo en zonas con offset negativo, como
     * Costa Rica).
     */
    private fun hoyNormalizado(): Long {
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}