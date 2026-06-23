package com.example.billingapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val subtotal: Double,
    val gst: Double,
    val totalAmount: Double,
    val paymentMethod: String,
    val invoiceNumber: String
)

@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: String,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val imageUrl: String? = null
)
