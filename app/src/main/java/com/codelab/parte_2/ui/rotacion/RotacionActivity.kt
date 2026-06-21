package com.codelab.parte_2.ui.rotacion

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.codelab.parte_2.R
import com.codelab.parte_2.data.local.AppDatabase
import com.codelab.parte_2.data.local.repositories.EstadoRotacionRepository
import com.codelab.parte_2.data.local.repositories.PotreroRepository
import com.codelab.parte_2.databinding.ActivityRotacionBinding
import com.codelab.parte_2.entity.EstadoRotacion
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla 3 — estado y rotación de potreros. RecyclerView con color
 * según estado vigente en la fecha seleccionada, selector de fecha
 * (MaterialDatePicker), y botón para cargar con ganado los potreros
 * marcados (solo los que están en VERDE se pueden marcar).
 */
class RotacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRotacionBinding
    private lateinit var adapter: RotacionAdapter

    private val viewModel: RotacionViewModel by viewModels {
        val db = AppDatabase.getInstance(applicationContext)
        RotacionViewModelFactory(
            PotreroRepository(db.potreroDao()),
            EstadoRotacionRepository(db.estadoRotacionDao())
        )
    }

    // Todas las fechas de Pantalla 3 (la "fecha seleccionada", fechaInicio,
    // fechaFin) se manejan en UTC, igual que MaterialDatePicker. Por eso el
    // formateador también debe leerlas en UTC: si usara la zona local del
    // dispositivo, en Costa Rica (UTC-6) "hoy" se vería como el día
    // anterior a las 6pm.
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRotacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupDatePicker()
        setupBotonCargarGanado()
        observarFecha()
        observarPotreros()
    }

    private fun setupRecyclerView() {
        adapter = RotacionAdapter(
            onToggleSeleccion = { potreroId -> viewModel.toggleSeleccion(potreroId) },
            onSolicitarHistorico = { potreroId, callback -> cargarHistorico(potreroId, callback) }
        )
        binding.recyclerRotacion.layoutManager = LinearLayoutManager(this)
        binding.recyclerRotacion.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.btnSeleccionarFecha.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.titulo_seleccionar_fecha))
                .setSelection(viewModel.fechaSeleccionada.value)
                .build()

            picker.addOnPositiveButtonClickListener { seleccionUtc ->
                viewModel.seleccionarFecha(seleccionUtc)
            }
            picker.show(supportFragmentManager, "date_picker_rotacion")
        }
    }

    private fun setupBotonCargarGanado() {
        binding.btnCargarGanado.setOnClickListener {
            viewModel.cargarGanado { exito ->
                val mensaje = if (exito) {
                    getString(R.string.msg_cargar_ganado_ok)
                } else {
                    getString(R.string.msg_cargar_ganado_vacio)
                }
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun observarFecha() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fechaSeleccionada.collect { fecha ->
                    binding.textFechaSeleccionada.text =
                        getString(R.string.fecha_seleccionada_format, dateFormat.format(Date(fecha)))
                }
            }
        }
    }

    private fun observarPotreros() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.potrerosConEstado.collect { lista ->
                    adapter.submitList(lista)
                    binding.textEmpty.visibility =
                        if (lista.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                }
            }
        }
    }

    private fun cargarHistorico(potreroId: Int, callback: (List<EstadoRotacion>) -> Unit) {
        lifecycleScope.launch {
            viewModel.obtenerHistorico(potreroId).collect { historico ->
                callback(historico)
            }
        }
    }
}