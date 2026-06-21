package com.codelab.parte_2.ui.rotacion

import com.codelab.parte_2.entity.EstadoColor
import com.codelab.parte_2.entity.Potrero

data class PotreroConEstado(
    val potrero: Potrero,
    val colorActual: EstadoColor,
    val seleccionado: Boolean = false
)