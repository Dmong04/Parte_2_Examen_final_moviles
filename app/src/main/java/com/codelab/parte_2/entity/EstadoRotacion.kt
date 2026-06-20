package com.codelab.parte_2.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "estado_rotacion",
    foreignKeys = [
        ForeignKey(
            entity = Potrero::class,
            parentColumns = ["id"],
            childColumns = ["potreroId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("potreroId")]
)
data class EstadoRotacion(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val potreroId: Int,
    val color: EstadoColor,
    val fechaInicio: Long,
    val fechaFin: Long? = null
)