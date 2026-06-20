package com.codelab.parte_2.data.local

import androidx.room.TypeConverter
import com.codelab.parte_2.entity.EstadoColor

class Converters {
    @TypeConverter
    fun fromEstadoColor(color: EstadoColor): String = color.name

    @TypeConverter
    fun toEstadoColor(value: String): EstadoColor = EstadoColor.valueOf(value)
}