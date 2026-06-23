package com.example.billingapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.billingapp.data.local.AppDatabase
import com.example.billingapp.data.repository.ProductRepositoryImpl
import com.example.billingapp.data.repository.SaleRepositoryImpl
import com.example.billingapp.domain.usecase.*

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillingViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            val productRepo = ProductRepositoryImpl(database.productDao())
            val saleRepo = SaleRepositoryImpl(database.saleDao())
            
            @Suppress("UNCHECKED_CAST")
            return BillingViewModel(
                GetProductsUseCase(productRepo),
                GetProductByBarcodeUseCase(productRepo),
                AddProductUseCase(productRepo),
                GetSalesUseCase(saleRepo),
                AddSaleUseCase(saleRepo)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
