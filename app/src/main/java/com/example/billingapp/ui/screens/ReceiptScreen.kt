package com.example.billingapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billingapp.ui.viewmodel.BillingViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    saleId: String,
    viewModel: BillingViewModel,
    onNavigateBack: () -> Unit
) {
    val sales by viewModel.salesHistory.collectAsState()
    val sale = sales.find { it.id == saleId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (sale == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Sale not found")
            }
        } else {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("My Store", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("123, Main Street, Your City", fontSize = 14.sp)
                Text("Phone: 9876543210", fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Invoice #", fontWeight = FontWeight.Bold)
                    Text(sale.invoiceNumber)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Date", fontWeight = FontWeight.Bold)
                    Text(dateFormat.format(Date(sale.date)))
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                sale.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.productName} (x${item.quantity})", modifier = Modifier.weight(1f))
                        Text("₹${String.format(Locale.getDefault(), "%.2f", item.price * item.quantity)}")
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal")
                    Text("₹${String.format(Locale.getDefault(), "%.2f", sale.subtotal)}")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GST (5%)")
                    Text("₹${String.format(Locale.getDefault(), "%.2f", sale.gst)}")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("₹${String.format(Locale.getDefault(), "%.2f", sale.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                Text("Thank you!", fontWeight = FontWeight.Medium)
                Text("Visit Again", fontSize = 12.sp)
            }
        }
    }
}
