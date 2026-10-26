package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ExpenditureDao
import com.example.data.dao.UserDao
import com.example.data.dao.VehicleEntryDao
import com.example.data.model.ExpenditureEntity
import com.example.data.model.UserEntity
import com.example.data.model.VehicleEntryEntity

@Database(
    entities = [
        UserEntity::class,
        VehicleEntryEntity::class,
        ExpenditureEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun vehicleEntryDao(): VehicleEntryDao
    abstract fun expenditureDao(): ExpenditureDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dash_pollution_puc.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
