package com.codelab.parte_2.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelab.parte_2.data.local.repositories.PotreroRepository
import com.codelab.parte_2.entity.Potrero
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PotreroListViewModel(
    private val repository: PotreroRepository
) : ViewModel() {

    // Lista en vivo de potreros — Room emite automáticamente cada vez que cambia la tabla
    val potreros: StateFlow<List<Potrero>> = repository.obtenerTodos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun eliminar(potrero: Potrero) {
        viewModelScope.launch {
            // Borra el potrero; por el ForeignKey.CASCADE en EstadoRotacion y
            // MedidaHistorica, Room borra en cascada su historial (requisito 4.c)
            repository.eliminar(potrero)
        }
    }
}
