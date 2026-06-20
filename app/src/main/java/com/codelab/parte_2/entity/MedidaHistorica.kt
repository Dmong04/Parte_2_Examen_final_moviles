package com.codelab.parte_2.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medida_historica",
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
data class MedidaHistorica(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val potreroId: Int,
    val medidaM2: Double,
    val fechaRegistro: Long
)