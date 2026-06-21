package com.codelab.parte_2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codelab.parte_2.databinding.ActivityFormPotreroBinding

/**
 * Pantalla 2 — PLACEHOLDER.
 *
 * Esto es solo para que Pantalla 1 pueda navegar y el proyecto compile.
 * Mendo: acá va el formulario real (nombre, medida m², fecha de creación,
 * foto/video, validaciones e insert/update contra PotreroRepository).
 *
 * Si potreroId llega como -1 es alta de un potrero nuevo; si llega un id
 * real es edición (cargar el potrero con repository.obtenerPorId(id)).
 */
class FormPotreroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFormPotreroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val potreroId = intent.getIntExtra(EXTRA_POTRERO_ID, -1)
        binding.textPlaceholder.text = if (potreroId != -1) {
            "TODO (Mendo): formulario de edición — potreroId=$potreroId"
        } else {
            "TODO (Mendo): formulario de nuevo potrero"
        }
    }

    companion object {
        const val EXTRA_POTRERO_ID = "extra_potrero_id"
    }
}
