# Firebase Service Account Setup Guide

## Step 1: Generate Service Account Key

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Click the gear icon (⚙️) → Project Settings
4. Go to the "Service accounts" tab
5. Click "Generate new private key"
6. Download the JSON file and save it securely on your server

## Step 2: Database Schema for FCM Tokens

Add this to your database schema:

```sql
-- For users table, add FCM token column
ALTER TABLE users ADD COLUMN fcm_token VARCHAR(255);
ALTER TABLE users ADD COLUMN device_id VARCHAR(100);
ALTER TABLE users ADD COLUMN token_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Or create separate table for multiple devices per user
CREATE TABLE user_fcm_tokens (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(50) NOT NULL,
    fcm_token VARCHAR(255) NOT NULL,
    device_id VARCHAR(100),
    device_type VARCHAR(20) DEFAULT 'android',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY unique_user_device (user_id, device_id)
);
```

## Step 3: Backend API Endpoints

Create these endpoints in your backend:

### POST /api/users/fcm-token

```json
{
	"userId": "user123",
	"fcmToken": "fcm_token_here",
	"deviceId": "device_id_optional"
}
```

### PUT /api/users/fcm-token

```json
{
	"userId": "user123",
	"fcmToken": "new_fcm_token_here",
	"deviceId": "device_id_optional"
}
```

## Step 4: Order Status Update Endpoint

### PUT /api/orders/{orderId}/status

```json
{
	"status": "shipped",
	"notifyUser": true
}
```

When this endpoint is called, it should:

1. Update the order status in database
2. Get the user's FCM token
3. Send push notification using Firebase Admin SDK

## Step 5: Testing

1. Update an order status from your website/admin panel
2. Check if the mobile app receives the notification
3. Check server logs for FCM send success/failure

## Common Status Values

- "pending" → "Order Confirmed"
- "processing" → "Order is Being Processed"
- "shipped" → "Order has been Shipped"
- "out_for_delivery" → "Order is Out for Delivery"
- "delivered" → "Order Delivered"
- "cancelled" → "Order Cancelled"

## Error Handling

- Handle invalid FCM tokens (user uninstalled app)
- Log all notification attempts
- Retry failed notifications
- Clean up invalid tokens from database
