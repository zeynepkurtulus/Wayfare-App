package com.zeynekurtulus.wayfare.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.zeynekurtulus.wayfare.data.local.converters.RouteConverters
import com.zeynekurtulus.wayfare.data.local.dao.RouteDao
import com.zeynekurtulus.wayfare.data.local.entities.RouteEntity

@Database(
    entities = [RouteEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RouteConverters::class)
abstract class WayfareDatabase : RoomDatabase() {
    
    abstract fun routeDao(): RouteDao
    
    companion object {
        @Volatile
        private var INSTANCE: WayfareDatabase? = null
        
        fun getDatabase(context: Context): WayfareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WayfareDatabase::class.java,
                    "wayfare_database"
                )
                .fallbackToDestructiveMigration() // For development, remove in production
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}