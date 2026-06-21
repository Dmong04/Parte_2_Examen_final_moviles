package com.codelab.parte_2.ui.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codelab.parte_2.data.local.repositories.EstadoRotacionRepository
import com.codelab.parte_2.data.local.repositories.MedidaHistoricaRepository
import com.codelab.parte_2.data.local.repositories.PotreroRepository

class FormPotreroViewModelFactory(
    private val potreroRepository: PotreroRepository,
    private val estadoRotacionRepository: EstadoRotacionRepository,
    private val medidaHistoricaRepository: MedidaHistoricaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FormPotreroViewModel::class.java)) {
            return FormPotreroViewModel(
                potreroRepository,
                estadoRotacionRepository,
                medidaHistoricaRepository
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
