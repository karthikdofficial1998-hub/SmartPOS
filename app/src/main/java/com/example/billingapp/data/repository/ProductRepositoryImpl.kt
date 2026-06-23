package com.example.billingapp.data.repository

import com.example.billingapp.data.local.dao.ProductDao
import com.example.billingapp.data.local.entity.toDomain
import com.example.billingapp.data.local.entity.toEntity
import com.example.billingapp.domain.model.Product
import com.example.billingapp.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(private val productDao: ProductDao) : ProductRepository {
    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProductByBarcode(barcode: String): Product? {
        return productDao.getProductByBarcode(barcode)?.toDomain()
    }

    override suspend fun addProduct(product: Product) {
        productDao.insertProduct(product.toEntity())
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toEntity())
    }

    override suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product.toEntity())
    }
}
