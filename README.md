# UPI Payments Android App

A runnable Android starter for collecting UPI payments with a QR section, payment form, status state, and transaction history.

## Current behavior

- Jetpack Compose UI
- Amount and note input
- Demo payment adapter with success/failure state handling
- Recent transaction list
- Provider abstraction ready for Razorpay or Cashfree

## Run locally

1. Open `upi-payments-app` in Android Studio Hedgehog or newer.
2. Let Gradle sync.
3. Run on an Android 8.0+ emulator or device.

## Going live

The app currently uses `DemoPaymentProvider`, so it does not move real money. Replace it with a server-backed provider integration. Never put provider secret keys in the Android app. The production setup should be:

`Android app → your backend → Razorpay/Cashfree → webhook → backend → app status`

You will need a merchant account, webhook endpoint, server-side signature verification, and a real UPI ID/QR payload.
