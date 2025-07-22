package com.example.book_souls_project;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.book_souls_project.api.ApiClient;
import com.example.book_souls_project.api.service.UserService;
import com.example.book_souls_project.api.types.user.FCMTokenRequest;
import com.example.book_souls_project.util.TokenManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Message received with data: " + remoteMessage.getData());

        // Check if message contains a data payload
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Check if message contains a notification payload
        /*if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            showNotification(title != null ? title : "Notification", body != null ? body : "");
        }*/
    }
    // [END receive_message]

    private void handleDataMessage(java.util.Map<String, String> data) {
        Log.d(TAG, "Handling data message: " + data);

        // Extract common fields
        String title = data.get("title");
        String body = data.get("body");
        String type = data.get("type");

        // Handle different notification types
        if (type != null) {
            Log.d(TAG, "Notification type: " + type);

            switch (type) {
                case "order_status_update":
                    handleOrderStatusUpdate(data, title, body);
                    break;
                case "new_order":
                    handleNewOrder(data, title, body);
                    break;
                case "payment_confirmation":
                    handlePaymentConfirmation(data, title, body);
                    break;
                default:
                    // Default notification handling
                    if (title != null && body != null) {
                        showNotification(title, body);
                    }
                    break;
            }
        } else {
            // Default handling if no type specified
            if (title != null && body != null) {
                showNotification(title, body);
            }
        }
    }

    private void handleOrderStatusUpdate(java.util.Map<String, String> data, String title, String body) {
        String orderId = data.get("orderId");
        String status = data.get("status");
        String userId = data.get("userId");

        Log.d(TAG, "Order status update - Order: " + orderId + ", Status: " + status + ", User: " + userId);

        // Create enhanced notification for order status
        String notificationTitle = title != null ? title : "Order Status Updated";
        String notificationBody = body != null ? body :
            String.format("Your order #%s is now %s", orderId, status);

        showOrderNotification(notificationTitle, notificationBody, orderId, status);
    }

    private void handleNewOrder(java.util.Map<String, String> data, String title, String body) {
        // Handle new order notifications (for admin/store users)
        Log.d(TAG, "New order notification received");
        showNotification(title != null ? title : "New Order",
                        body != null ? body : "You have a new order");
    }

    private void handlePaymentConfirmation(java.util.Map<String, String> data, String title, String body) {
        // Handle payment confirmation notifications
        Log.d(TAG, "Payment confirmation notification received");
        showNotification(title != null ? title : "Payment Confirmed",
                        body != null ? body : "Your payment has been processed");
    }

    // [START on_new_token]
    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any
     * server-side account maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        Log.d(TAG, "Sending token to server: " + token);

        try {
            // Get user info from TokenManager
            TokenManager tokenManager = new TokenManager(this);
            String authToken = tokenManager.getAccessToken();

            if (authToken != null) {
                // Create API client and service
                ApiClient apiClient = ApiClient.getInstance(this);
                UserService userService = apiClient.getRetrofit().create(UserService.class);

                // Send to server
                userService.updateFCMToken("Bearer " + authToken, token).enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "FCM token sent to server successfully");
                        } else {
                            Log.e(TAG, "Failed to send FCM token to server: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                        Log.e(TAG, "Error sending FCM token to server", t);
                    }
                });
            } else {
                Log.w(TAG, "User not logged in, cannot send FCM token to server");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while sending FCM token to server", e);
        }
    }

    private void askNotificationPermission() {
        // Note: Cannot request permissions from a service context
        // Permission should be requested from MainActivity or other Activity
        Log.w(TAG, "Notification permission not granted. Please request permission from an Activity.");
    }

    private void showNotification(String title, String message) {
        Log.d(TAG, "Showing notification - Title: " + title + ", Message: " + message);

        // Check if notification permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification permission not granted. Cannot show notification.");
                return;
            }
        }

        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "FCM_CHANNEL")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify((int) System.currentTimeMillis(), builder.build());
                Log.d(TAG, "Notification displayed successfully");
            } else {
                Log.e(TAG, "NotificationManager is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification", e);
        }
    }

    private void showOrderNotification(String title, String message, String orderId, String status) {
        Log.d(TAG, "Showing order notification - Title: " + title + ", Message: " + message + ", Order: " + orderId);

        // Check if notification permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification permission not granted. Cannot show notification.");
                return;
            }
        }

        try {
            // Create intent to open orders page when notification is tapped
            android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
            intent.putExtra("navigate_to", "orders");
            intent.putExtra("order_id", orderId);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);

            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                this,
                orderId.hashCode(), // Use orderId hash as unique request code
                intent,
                android.app.PendingIntent.FLAG_ONE_SHOT | android.app.PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "FCM_CHANNEL")
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setContentIntent(pendingIntent);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(orderId.hashCode(), builder.build());
                Log.d(TAG, "Order notification displayed successfully");
            } else {
                Log.e(TAG, "NotificationManager is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing order notification", e);
        }
    }
}
