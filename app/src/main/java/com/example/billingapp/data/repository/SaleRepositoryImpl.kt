package com.example.billingapp.data.repository

import com.example.billingapp.data.local.dao.SaleDao
import com.example.billingapp.data.local.entity.SaleEntity
import com.example.billingapp.data.local.entity.SaleItemEntity
import com.example.billingapp.domain.model.Sale
import com.example.billingapp.domain.model.SaleItem
import com.example.billingapp.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SaleRepositoryImpl(private val saleDao: SaleDao) : SaleRepository {
    override fun getAllSales(): Flow<List<Sale>> {
        return saleDao.getAllSales().map { list ->
            list.map { saleWithItems ->
                Sale(
                    id = saleWithItems.sale.id,
                    date = saleWithItems.sale.date,
                    items = saleWithItems.items.map { 
                        SaleItem(it.productName, it.quantity, it.price, it.imageUrl)
                    },
                    subtotal = saleWithItems.sale.subtotal,
                    gst = saleWithItems.sale.gst,
                    totalAmount = saleWithItems.sale.totalAmount,
                    paymentMethod = saleWithItems.sale.paymentMethod,
                    invoiceNumber = saleWithItems.sale.invoiceNumber
                )
            }
        }
    }

    override suspend fun addSale(sale: Sale) {
        val saleEntity = SaleEntity(
            id = sale.id,
            date = sale.date,
            subtotal = sale.subtotal,
            gst = sale.gst,
            totalAmount = sale.totalAmount,
            paymentMethod = sale.paymentMethod,
            invoiceNumber = sale.invoiceNumber
        )
        val itemEntities = sale.items.map { 
            SaleItemEntity(
                saleId = sale.id,
                productName = it.productName,
                quantity = it.quantity,
                price = it.price,
                imageUrl = it.imageUrl
            )
        }
        saleDao.insertSaleWithItems(saleEntity, itemEntities)
    }
}
