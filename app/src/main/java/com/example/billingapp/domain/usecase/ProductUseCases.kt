package com.example.billingapp.domain.usecase

import com.example.billingapp.domain.model.Product
import com.example.billingapp.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class GetProductsUseCase(private val repository: ProductRepository) {
    operator fun invoke(): Flow<List<Product>> = repository.getAllProducts()
}

class GetProductByBarcodeUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(barcode: String): Product? = repository.getProductByBarcode(barcode)
}

class AddProductUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(product: Product) = repository.addProduct(product)
}

class UpdateProductUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(product: Product) = repository.updateProduct(product)
}

class DeleteProductUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(product: Product) = repository.deleteProduct(product)
}
