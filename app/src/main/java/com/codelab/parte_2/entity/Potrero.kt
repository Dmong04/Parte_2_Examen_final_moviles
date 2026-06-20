package com.codelab.parte_2.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "potrero")
data class Potrero(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val medidaM2: Double,
    val fechaCreacion: Long,
    val fotoUri: String? = null,
    val videoUri: String? = null
)