package com.example.upipayments

import kotlinx.coroutines.delay

interface PaymentProvider {
    suspend fun createPayment(amount: Double, note: String): PaymentResult
}

data class PaymentResult(
    val success: Boolean,
    val transactionId: String,
    val message: String
)

/** Replace this adapter with Razorpay/Cashfree once merchant credentials are available. */
class DemoPaymentProvider : PaymentProvider {
    override suspend fun createPayment(amount: Double, note: String): PaymentResult {
        delay(900)
        return PaymentResult(
            success = true,
            transactionId = "DEMO-${System.currentTimeMillis()}",
            message = "Demo payment approved"
        )
    }
}
