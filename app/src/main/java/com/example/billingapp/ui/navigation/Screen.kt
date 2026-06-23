package com.example.billingapp.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Scanner : Screen("scanner")
    data object ProductDetails : Screen("product_details/{barcode}") {
        fun createRoute(barcode: String) = "product_details/$barcode"
    }
    data object Cart : Screen("cart")
    data object Checkout : Screen("checkout")
    data object PaymentSuccess : Screen("payment_success/{saleId}") {
        fun createRoute(saleId: String) = "payment_success/$saleId"
    }
    data object Receipt : Screen("receipt/{saleId}") {
        fun createRoute(saleId: String) = "receipt/$saleId"
    }
    data object SalesHistory : Screen("sales_history")
    data object Settings : Screen("settings")
}
