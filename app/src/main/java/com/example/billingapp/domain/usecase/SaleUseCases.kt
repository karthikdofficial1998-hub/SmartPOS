package com.example.billingapp.domain.usecase

import com.example.billingapp.domain.model.Sale
import com.example.billingapp.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow

class GetSalesUseCase(private val repository: SaleRepository) {
    operator fun invoke(): Flow<List<Sale>> = repository.getAllSales()
}

class AddSaleUseCase(private val repository: SaleRepository) {
    suspend operator fun invoke(sale: Sale) = repository.addSale(sale)
}
