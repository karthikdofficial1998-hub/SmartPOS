package com.example.billingapp.domain.model

data class Product(
    val id: Long = 0,
    val barcode: String,
    val name: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String? = null
)
