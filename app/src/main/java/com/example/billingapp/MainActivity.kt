package com.example.billingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.billingapp.ui.navigation.Screen
import com.example.billingapp.ui.screens.*
import com.example.billingapp.ui.theme.BillingAPPTheme
import com.example.billingapp.ui.viewmodel.BillingViewModel
import com.example.billingapp.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BillingAPPTheme {
                val navController = rememberNavController()
                val viewModel: BillingViewModel = viewModel(
                    factory = ViewModelFactory(applicationContext)
                )

                // Initialize dummy data for testing
                LaunchedEffect(Unit) {
                    viewModel.addDummyProducts()
                }

                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route
                ) {
                    composable(Screen.Splash.route) {
                        SplashScreen(onNext = {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        })
                    }
                    composable(Screen.Onboarding.route) {
                        OnboardingScreen(onFinish = {
                            navController.navigate(Screen.Scanner.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        })
                    }
                    composable(Screen.Scanner.route) {
                        ScannerScreen(
                            viewModel = viewModel,
                            onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                            onNavigateToDetails = { barcode -> 
                                navController.navigate(Screen.ProductDetails.createRoute(barcode)) 
                            }
                        )
                    }
                    composable(Screen.ProductDetails.route) { backStackEntry ->
                        val barcode = backStackEntry.arguments?.getString("barcode") ?: ""
                        ProductDetailsScreen(
                            barcode = barcode,
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.Cart.route) {
                        CartScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToCheckout = { navController.navigate(Screen.Checkout.route) }
                        )
                    }
                    composable(Screen.Checkout.route) {
                        CheckoutScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onPaymentSuccess = { 
                                val saleId = viewModel.salesHistory.value.firstOrNull()?.id ?: ""
                                navController.navigate(Screen.PaymentSuccess.createRoute(saleId)) {
                                    popUpTo(Screen.Scanner.route) { inclusive = false }
                                }
                            }
                        )
                    }
                    composable(Screen.PaymentSuccess.route) { backStackEntry ->
                        val saleId = backStackEntry.arguments?.getString("saleId") ?: ""
                        PaymentSuccessScreen(
                            saleId = saleId,
                            onViewReceipt = { navController.navigate(Screen.Receipt.createRoute(saleId)) },
                            onBackToHome = { 
                                navController.navigate(Screen.Scanner.route) {
                                    popUpTo(Screen.Scanner.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(Screen.Receipt.route) { backStackEntry ->
                        val saleId = backStackEntry.arguments?.getString("saleId") ?: ""
                        ReceiptScreen(
                            saleId = saleId,
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(Screen.SalesHistory.route) {
                        SalesHistoryScreen(
                            viewModel = viewModel,
                            onNavigateToScanner = { navController.navigate(Screen.Scanner.route) },
                            onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                            onNavigateToSettings = { /* navController.navigate(Screen.Settings.route) */ }
                        )
                    }
                    composable(Screen.Settings.route) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Settings coming soon...")
                        }
                    }
                }
            }
        }
    }
}
