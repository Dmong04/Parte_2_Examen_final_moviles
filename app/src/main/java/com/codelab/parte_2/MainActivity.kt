package com.codelab.parte_2

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.codelab.parte_2.data.local.AppDatabase
import com.codelab.parte_2.data.local.repositories.PotreroRepository
import com.codelab.parte_2.databinding.ActivityMainBinding
import com.codelab.parte_2.entity.Potrero
import com.codelab.parte_2.ui.main.PotreroAdapter
import com.codelab.parte_2.ui.main.PotreroListViewModel
import com.codelab.parte_2.ui.main.PotreroListViewModelFactory
import kotlinx.coroutines.launch
import com.codelab.parte_2.ui.rotacion.RotacionActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PotreroAdapter

    private val viewModel: PotreroListViewModel by viewModels {
        val dao = AppDatabase.getInstance(applicationContext).potreroDao()
        PotreroListViewModelFactory(PotreroRepository(dao))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFab()
        observePotreros()
    }

    private fun setupRecyclerView() {
        adapter = PotreroAdapter(
            onClick = { potrero -> abrirFormulario(potrero) },
            onLongClick = { potrero -> confirmarEliminacion(potrero) }
        )
        binding.recyclerPotreros.layoutManager = LinearLayoutManager(this)
        binding.recyclerPotreros.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddPotrero.setOnClickListener {
            abrirFormulario(potrero = null)
        }
        binding.fabRotacion.setOnClickListener {
            startActivity(Intent(this, RotacionActivity::class.java))
        }
    }

    private fun observePotreros() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.potreros.collect { lista ->
                    adapter.submitList(lista)
                    binding.textEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun abrirFormulario(potrero: Potrero?) {
        val intent = Intent(this, FormPotreroActivity::class.java)
        if (potrero != null) {
            intent.putExtra(FormPotreroActivity.EXTRA_POTRERO_ID, potrero.id)
        }
        startActivity(intent)
    }

    private fun confirmarEliminacion(potrero: Potrero) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, potrero.nombre))
            .setPositiveButton(R.string.dialog_delete_positive) { _, _ ->
                viewModel.eliminar(potrero)
            }
            .setNegativeButton(R.string.dialog_delete_negative, null)
            .show()
    }
}
