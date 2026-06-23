package com.example.billingapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billingapp.ui.viewmodel.BillingViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: BillingViewModel,
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()
    val subtotal = cartItems.sumOf { it.totalPrice }
    val gst = subtotal * 0.05
    val total = subtotal + gst

    val paymentOptions = listOf("Cash", "UPI", "Card")
    var selectedOption by remember { mutableStateOf(paymentOptions[0]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.checkout(selectedOption)
                    onPaymentSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Place Order")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Bill Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            
            SummaryRow("Items", cartItems.size.toString())
            SummaryRow("Subtotal", "₹${String.format(Locale.getDefault(), "%.2f", subtotal)}")
            SummaryRow("GST (5%)", "₹${String.format(Locale.getDefault(), "%.2f", gst)}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            SummaryRow("Total Amount", "₹${String.format(Locale.getDefault(), "%.2f", total)}", isTotal = true)

            Spacer(modifier = Modifier.height(32.dp))
            Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Column(Modifier.selectableGroup()) {
                paymentOptions.forEach { text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (text == selectedOption),
                                onClick = { selectedOption = text },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == selectedOption),
                            onClick = null // null recommended for accessibility with screenreaders
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isTotal) 18.sp else 16.sp
        )
        Text(
            text = value,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isTotal) 18.sp else 16.sp,
            color = if (isTotal) MaterialTheme.colorScheme.primary else Color.Unspecified
        )
    }
}
