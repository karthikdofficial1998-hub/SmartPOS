package com.example.billingapp.domain.repository

import com.example.billingapp.domain.model.Sale
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    fun getAllSales(): Flow<List<Sale>>
    suspend fun addSale(sale: Sale)
}
