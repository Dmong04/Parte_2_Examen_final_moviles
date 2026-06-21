package com.codelab.parte_2.ui.rotacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.codelab.parte_2.data.local.repositories.EstadoRotacionRepository
import com.codelab.parte_2.data.local.repositories.PotreroRepository

class RotacionViewModelFactory(
    private val potreroRepository: PotreroRepository,
    private val estadoRotacionRepository: EstadoRotacionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RotacionViewModel(potreroRepository, estadoRotacionRepository) as T
    }
}