package com.example.billingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billingapp.domain.model.CartItem
import com.example.billingapp.domain.model.Product
import com.example.billingapp.domain.model.Sale
import com.example.billingapp.domain.model.SaleItem
import com.example.billingapp.domain.usecase.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class BillingViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val getSalesUseCase: GetSalesUseCase,
    private val addSaleUseCase: AddSaleUseCase
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _scannedProduct = MutableStateFlow<Product?>(null)
    val scannedProduct: StateFlow<Product?> = _scannedProduct.asStateFlow()

    private val _lastScannedBarcode = MutableStateFlow<String?>(null)
    val lastScannedBarcode: StateFlow<String?> = _lastScannedBarcode.asStateFlow()

    private val _salesHistory = MutableStateFlow<List<Sale>>(emptyList())
    val salesHistory: StateFlow<List<Sale>> = _salesHistory.asStateFlow()

    private val _scanEvent = MutableSharedFlow<ScanEvent>()
    val scanEvent: SharedFlow<ScanEvent> = _scanEvent.asSharedFlow()

    sealed class ScanEvent {
        data class Success(val product: Product) : ScanEvent()
        data class Unknown(val barcode: String) : ScanEvent()
    }

    private var lastScanTime = 0L
    private val scanCooldown = 1500L // 1.5 seconds cooldown for the same barcode

    init {
        loadSalesHistory()
    }

    private fun loadSalesHistory() {
        viewModelScope.launch {
            getSalesUseCase().collect {
                _salesHistory.value = it
            }
        }
    }

    fun scanBarcode(barcode: String) {
        val currentTime = System.currentTimeMillis()
        
        // Cooldown logic: if it's the same barcode, wait for cooldown
        if (barcode == _lastScannedBarcode.value && (currentTime - lastScanTime) < scanCooldown) {
            return
        }

        android.util.Log.d("BillingViewModel", "Processing barcode: $barcode")
        lastScanTime = currentTime
        _lastScannedBarcode.value = barcode

        viewModelScope.launch {
            val product = getProductByBarcodeUseCase(barcode)
            _scannedProduct.value = product
            if (product != null) {
                _scanEvent.emit(ScanEvent.Success(product))
            } else {
                _scanEvent.emit(ScanEvent.Unknown(barcode))
            }
        }
    }

    fun clearScannedProduct() {
        _scannedProduct.value = null
        _lastScannedBarcode.value = null
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst { it.product.barcode == product.barcode }
        
        if (existingItemIndex != -1) {
            val existingItem = currentItems[existingItemIndex]
            currentItems[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            currentItems.add(CartItem(product, quantity))
        }
        _cartItems.value = currentItems
    }

    fun removeFromCart(product: Product) {
        val currentItems = _cartItems.value.filter { it.product.barcode != product.barcode }
        _cartItems.value = currentItems
    }

    fun updateCartQuantity(product: Product, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(product)
            return
        }
        val currentItems = _cartItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.product.barcode == product.barcode }
        if (index != -1) {
            currentItems[index] = currentItems[index].copy(quantity = quantity)
            _cartItems.value = currentItems
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            addProductUseCase(product)
            // If the product added is the one currently being scanned, update the scannedProduct state
            if (_lastScannedBarcode.value == product.barcode) {
                _scannedProduct.value = product
            }
        }
    }

    fun checkout(paymentMethod: String) {
        viewModelScope.launch {
            val items = _cartItems.value
            if (items.isEmpty()) return@launch

            val subtotal = items.sumOf { it.totalPrice }
            val gst = subtotal * 0.05 // 5% GST
            val total = subtotal + gst
            val saleId = UUID.randomUUID().toString()
            val invoiceNumber = "INV${System.currentTimeMillis()}"

            val sale = Sale(
                id = saleId,
                date = System.currentTimeMillis(),
                items = items.map { SaleItem(it.product.name, it.quantity, it.product.price, it.product.imageUrl) },
                subtotal = subtotal,
                gst = gst,
                totalAmount = total,
                paymentMethod = paymentMethod,
                invoiceNumber = invoiceNumber
            )

            addSaleUseCase(sale)
            clearCart()
        }
    }
    
    // For demo purposes: Add some dummy products if database is empty
    fun addDummyProducts() {
        viewModelScope.launch {
            val dummyProducts = listOf(
                Product(barcode = "8901030895432", name = "Milk - Amul Taaza", price = 30.0, stock = 45),
                Product(barcode = "8901719101038", name = "Lays Magic Masala", price = 20.0, stock = 100),
                Product(barcode = "2", name = "Bread - Brown", price = 40.0, stock = 20),
                Product(barcode = "3", name = "Coffee - Nescafe", price = 150.0, stock = 15)
            )
            dummyProducts.forEach { addProductUseCase(it) }
        }
    }
}
