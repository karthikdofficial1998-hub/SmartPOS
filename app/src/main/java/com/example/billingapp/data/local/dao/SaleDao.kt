package com.example.billingapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.billingapp.data.local.entity.SaleEntity
import com.example.billingapp.data.local.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SaleDao {
    @Transaction
    @Query("SELECT * FROM sales ORDER BY date DESC")
    abstract fun getAllSales(): Flow<List<SaleWithItems>>

    @Insert
    abstract suspend fun insertSale(sale: SaleEntity)

    @Insert
    abstract suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Transaction
    open suspend fun insertSaleWithItems(sale: SaleEntity, items: List<SaleItemEntity>) {
        insertSale(sale)
        insertSaleItems(items)
    }
}

data class SaleWithItems(
    @androidx.room.Embedded val sale: SaleEntity,
    @androidx.room.Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItemEntity>
)
