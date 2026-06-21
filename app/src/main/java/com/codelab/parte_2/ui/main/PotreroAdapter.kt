package com.codelab.parte_2.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codelab.parte_2.R
import com.codelab.parte_2.databinding.ItemPotreroBinding
import com.codelab.parte_2.entity.Potrero
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PotreroAdapter(
    private val onClick: (Potrero) -> Unit,
    private val onLongClick: (Potrero) -> Unit
) : ListAdapter<Potrero, PotreroAdapter.PotreroViewHolder>(DIFF_CALLBACK) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PotreroViewHolder {
        val binding = ItemPotreroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PotreroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PotreroViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PotreroViewHolder(
        private val binding: ItemPotreroBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(potrero: Potrero) {
            val context = binding.root.context
            binding.textNombre.text = potrero.nombre
            binding.textMedida.text = context.getString(R.string.potrero_medida_format, potrero.medidaM2)
            binding.textFecha.text = context.getString(
                R.string.potrero_fecha_format,
                dateFormat.format(Date(potrero.fechaCreacion))
            )

            binding.root.setOnClickListener { onClick(potrero) }
            binding.root.setOnLongClickListener {
                onLongClick(potrero)
                true
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Potrero>() {
            override fun areItemsTheSame(oldItem: Potrero, newItem: Potrero) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Potrero, newItem: Potrero) = oldItem == newItem
        }
    }
}
