package com.example.billingapp.domain.model

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val totalPrice: Double get() = product.price * quantity
}
