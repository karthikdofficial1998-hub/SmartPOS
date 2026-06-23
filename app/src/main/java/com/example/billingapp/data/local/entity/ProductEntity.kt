package com.example.billingapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.billingapp.domain.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val barcode: String,
    val name: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String? = null
)

fun ProductEntity.toDomain(): Product {
    return Product(id, barcode, name, price, stock, imageUrl)
}

fun Product.toEntity(): ProductEntity {
    return ProductEntity(id, barcode, name, price, stock, imageUrl)
}
