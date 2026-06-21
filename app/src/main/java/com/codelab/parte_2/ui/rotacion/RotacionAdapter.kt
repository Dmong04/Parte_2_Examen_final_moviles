package com.codelab.parte_2.ui.rotacion

import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.codelab.parte_2.R
import com.codelab.parte_2.databinding.ItemPotreroRotacionBinding
import com.codelab.parte_2.entity.EstadoColor
import com.codelab.parte_2.entity.EstadoRotacion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RotacionAdapter(
    private val onToggleSeleccion: (potreroId: Int) -> Unit,
    private val onSolicitarHistorico: (potreroId: Int, callback: (List<EstadoRotacion>) -> Unit) -> Unit
) : ListAdapter<PotreroConEstado, RotacionAdapter.RotacionViewHolder>(DIFF_CALLBACK) {

    // Mismo criterio que en RotacionActivity: todas las fechas de esta
    // pantalla son UTC, así que el formateador también debe leerlas en UTC.
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = java.util.TimeZone.getTimeZone("UTC")
    }

    // IDs de potreros cuya fila está expandida mostrando el histórico
    private val expandidos = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RotacionViewHolder {
        val binding = ItemPotreroRotacionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RotacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RotacionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RotacionViewHolder(
        private val binding: ItemPotreroRotacionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PotreroConEstado) {
            val context = binding.root.context
            val potrero = item.potrero

            binding.textNombre.text = potrero.nombre
            binding.textEstado.text = textoEstado(context, item.colorActual)

            pintarFondo(item.colorActual)

            binding.checkboxSeleccion.isEnabled = item.colorActual == EstadoColor.VERDE
            binding.checkboxSeleccion.setOnCheckedChangeListener(null)
            binding.checkboxSeleccion.isChecked = item.seleccionado
            binding.checkboxSeleccion.setOnCheckedChangeListener { _, _ ->
                onToggleSeleccion(potrero.id)
            }

            binding.root.setOnClickListener {
                if (item.colorActual == EstadoColor.VERDE) {
                    binding.checkboxSeleccion.toggle()
                }
            }

            binding.root.setOnLongClickListener {
                toggleExpandido(potrero.id)
                true
            }

            val expandido = expandidos.contains(potrero.id)
            binding.layoutHistorico.isVisible = expandido
            binding.iconExpandir.rotation = if (expandido) 180f else 0f

            if (expandido) {
                cargarHistorico(potrero.id)
            }
        }

        private fun pintarFondo(color: EstadoColor) {
            val colorRes = when (color) {
                EstadoColor.ROJO -> R.color.estado_rojo
                EstadoColor.ANARANJADO -> R.color.estado_anaranjado
                EstadoColor.VERDE -> R.color.estado_verde
            }
            binding.cardRoot.setCardBackgroundColor(
                binding.root.context.getColor(colorRes)
            )
        }

        private fun textoEstado(context: android.content.Context, color: EstadoColor): String {
            return when (color) {
                EstadoColor.ROJO -> context.getString(R.string.estado_rojo_label)
                EstadoColor.ANARANJADO -> context.getString(R.string.estado_anaranjado_label)
                EstadoColor.VERDE -> context.getString(R.string.estado_verde_label)
            }
        }

        private fun cargarHistorico(potreroId: Int) {
            binding.textHistorico.text = binding.root.context.getString(R.string.cargando_historico)
            onSolicitarHistorico(potreroId) { historico ->
                binding.textHistorico.text = if (historico.isEmpty()) {
                    binding.root.context.getString(R.string.sin_historico)
                } else {
                    historico.joinToString(separator = "\n") { estado ->
                        val inicio = dateFormat.format(Date(estado.fechaInicio))
                        val fin = estado.fechaFin?.let { dateFormat.format(Date(it)) }
                            ?: binding.root.context.getString(R.string.fecha_vigente)
                        "${estado.color}: $inicio → $fin"
                    }
                }
            }
        }
    }

    private fun toggleExpandido(potreroId: Int) {
        if (expandidos.contains(potreroId)) {
            expandidos.remove(potreroId)
        } else {
            expandidos.add(potreroId)
        }
        val index = currentList.indexOfFirst { it.potrero.id == potreroId }
        if (index != -1) notifyItemChanged(index)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PotreroConEstado>() {
            override fun areItemsTheSame(oldItem: PotreroConEstado, newItem: PotreroConEstado) =
                oldItem.potrero.id == newItem.potrero.id

            override fun areContentsTheSame(oldItem: PotreroConEstado, newItem: PotreroConEstado) =
                oldItem == newItem
        }
    }
}