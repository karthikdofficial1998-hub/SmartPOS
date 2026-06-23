package com.example.billingapp.domain.model

data class Sale(
    val id: String,
    val date: Long,
    val items: List<SaleItem>,
    val subtotal: Double,
    val gst: Double,
    val totalAmount: Double,
    val paymentMethod: String,
    val invoiceNumber: String
)

data class SaleItem(
    val productName: String,
    val quantity: Int,
    val price: Double,
    val imageUrl: String? = null
)
