package com.example.upipayments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PaymentState {
    data object Idle : PaymentState
    data object Processing : PaymentState
    data class Success(val result: PaymentResult) : PaymentState
    data class Failure(val message: String) : PaymentState
}

data class Transaction(val amount: Double, val id: String, val status: String)

class PaymentViewModel(
    private val provider: PaymentProvider = DemoPaymentProvider()
) : ViewModel() {
    private val _state = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val state: StateFlow<PaymentState> = _state.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    fun pay(amount: Double, note: String) {
        if (amount <= 0) return
        viewModelScope.launch {
            _state.value = PaymentState.Processing
            runCatching { provider.createPayment(amount, note) }
                .onSuccess { result ->
                    _state.value = if (result.success) PaymentState.Success(result) else PaymentState.Failure(result.message)
                    if (result.success) {
                        _transactions.value = listOf(Transaction(amount, result.transactionId, "Success")) + _transactions.value
                    }
                }
                .onFailure { _state.value = PaymentState.Failure(it.message ?: "Payment failed") }
        }
    }

    fun reset() { _state.value = PaymentState.Idle }
}
