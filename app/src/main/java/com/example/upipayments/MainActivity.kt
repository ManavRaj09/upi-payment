package com.example.upipayments

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UpiPaymentsTheme { PaymentApp() } }
    }
}

@Composable
fun PaymentApp(vm: PaymentViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val transactions by vm.transactions.collectAsState()
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("Order payment") }

    Scaffold(topBar = { TopAppBar(title = { Text("UPI Payments", fontWeight = FontWeight.Bold) }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text("Collect payments simply", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Create a QR and track payment status in one place", color = Color.Gray)
            }
            item { QrCard() }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Create payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                            modifier = Modifier.fillMaxWidth(), label = { Text("Amount") }, prefix = { Text("₹ ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true
                        )
                        OutlinedTextField(value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Note") }, singleLine = true)
                        Button(
                            onClick = { vm.pay(amount.toDoubleOrNull() ?: 0.0, note) },
                            modifier = Modifier.fillMaxWidth(), enabled = state !is PaymentState.Processing
                        ) {
                            if (state is PaymentState.Processing) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            else { Icon(Icons.Default.Send, null); Spacer(Modifier.size(8.dp)); Text("Simulate payment") }
                        }
                        StatusBanner(state, onDismiss = vm::reset)
                    }
                }
            }
            item { Text("Recent transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            if (transactions.isEmpty()) item { Text("No transactions yet", color = Color.Gray) }
            items(transactions) { tx -> TransactionRow(tx) }
            item { Text("Demo mode • Connect a payment provider before going live", color = Color.Gray, style = MaterialTheme.typography.bodySmall) }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun QrCard() {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(86.dp).background(Color.White, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Image(generateQrBitmap("upi://pay?pa=demo@upi&pn=Demo%20Merchant&cu=INR").asImageBitmap(), "UPI QR", Modifier.size(70.dp))
            }
            Spacer(Modifier.size(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Your UPI QR", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Share this QR to collect a payment", color = Color(0xFFD1D5DB))
                TextButton(onClick = {}) { Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.size(6.dp)); Text("Copy UPI ID") }
            }
        }
    }
}

private fun generateQrBitmap(payload: String): Bitmap {
    val matrix = MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, 320, 320)
    val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.RGB_565)
    for (x in 0 until matrix.width) for (y in 0 until matrix.height) {
        bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
    }
    return bitmap
}

@Composable
private fun StatusBanner(state: PaymentState, onDismiss: () -> Unit) {
    when (state) {
        is PaymentState.Success -> Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F7EE))) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16803C)); Spacer(Modifier.size(8.dp));
                Column(Modifier.weight(1f)) { Text("Payment successful", fontWeight = FontWeight.Bold); Text(state.result.transactionId, style = MaterialTheme.typography.bodySmall) }
                TextButton(onClick = onDismiss) { Text("Done") }
            }
        }
        is PaymentState.Failure -> Text(state.message, color = MaterialTheme.colorScheme.error)
        else -> Unit
    }
}

@Composable
private fun TransactionRow(tx: Transaction) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16803C))
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) { Text("Payment received", fontWeight = FontWeight.SemiBold); Text(tx.id, style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
            Text("₹${String.format("%.2f", tx.amount)}", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun UpiPaymentsTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = { Surface(color = Color(0xFFF7F9FC)) { content() } })
}
