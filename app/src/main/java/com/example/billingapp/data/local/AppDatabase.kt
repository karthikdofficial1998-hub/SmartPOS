package com.example.billingapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.billingapp.data.local.dao.ProductDao
import com.example.billingapp.data.local.dao.SaleDao
import com.example.billingapp.data.local.entity.ProductEntity
import com.example.billingapp.data.local.entity.SaleEntity
import com.example.billingapp.data.local.entity.SaleItemEntity

@Database(
    entities = [ProductEntity::class, SaleEntity::class, SaleItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "billing_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
