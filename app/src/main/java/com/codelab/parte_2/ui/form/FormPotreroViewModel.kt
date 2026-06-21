package com.codelab.parte_2.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelab.parte_2.data.local.repositories.EstadoRotacionRepository
import com.codelab.parte_2.data.local.repositories.MedidaHistoricaRepository
import com.codelab.parte_2.data.local.repositories.PotreroRepository
import com.codelab.parte_2.entity.EstadoColor
import com.codelab.parte_2.entity.EstadoRotacion
import com.codelab.parte_2.entity.MedidaHistorica
import com.codelab.parte_2.entity.Potrero
import kotlinx.coroutines.launch

class FormPotreroViewModel(
    private val potreroRepository: PotreroRepository,
    private val estadoRotacionRepository: EstadoRotacionRepository,
    private val medidaHistoricaRepository: MedidaHistoricaRepository
) : ViewModel() {

    suspend fun cargarPotrero(id: Int): Potrero? = potreroRepository.obtenerPorId(id)

    fun guardar(
        potreroId: Int,
        nombre: String,
        medidaM2: Double,
        fechaCreacion: Long,
        fotoUri: String?,
        videoUri: String?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val idFinal: Int

                if (potreroId == -1) {
                    val nuevoId = potreroRepository.insertar(
                        Potrero(
                            nombre = nombre,
                            medidaM2 = medidaM2,
                            fechaCreacion = fechaCreacion,
                            fotoUri = fotoUri,
                            videoUri = videoUri
                        )
                    )
                    idFinal = nuevoId.toInt()

                    // Contrato con Pantalla 3: todo potrero nuevo nace en
                    // estado VERDE (disponible), arrancando desde su fecha
                    // de creación, para que la rotación tenga historial
                    // desde el día 1.
                    estadoRotacionRepository.insertar(
                        EstadoRotacion(
                            potreroId = idFinal,
                            color = EstadoColor.VERDE,
                            fechaInicio = fechaCreacion,
                            fechaFin = null
                        )
                    )
                } else {
                    idFinal = potreroId
                    potreroRepository.actualizar(
                        Potrero(
                            id = potreroId,
                            nombre = nombre,
                            medidaM2 = medidaM2,
                            fechaCreacion = fechaCreacion,
                            fotoUri = fotoUri,
                            videoUri = videoUri
                        )
                    )
                }

                // Deja registro histórico de la medida cada vez que se
                // crea o actualiza el potrero (la tabla medida_historica
                // ya existía en el modelo justamente para esto).
                medidaHistoricaRepository.insertar(
                    MedidaHistorica(
                        potreroId = idFinal,
                        medidaM2 = medidaM2,
                        fechaRegistro = System.currentTimeMillis()
                    )
                )

                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}
