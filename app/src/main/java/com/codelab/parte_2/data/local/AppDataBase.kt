package com.codelab.parte_2.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.codelab.parte_2.data.local.dao.EstadoRotacionDao
import com.codelab.parte_2.data.local.dao.MedidaHistoricaDao
import com.codelab.parte_2.data.local.dao.PotreroDao
import com.codelab.parte_2.entity.EstadoRotacion
import com.codelab.parte_2.entity.MedidaHistorica
import com.codelab.parte_2.entity.Potrero

@Database(
    entities = [Potrero::class, EstadoRotacion::class, MedidaHistorica::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun potreroDao(): PotreroDao
    abstract fun estadoRotacionDao(): EstadoRotacionDao
    abstract fun medidaHistoricaDao(): MedidaHistoricaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rotacion_ganado_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}