// Add to pom.xml:
// <dependency>
//     <groupId>com.google.firebase</groupId>
//     <artifactId>firebase-admin</artifactId>
//     <version>9.2.0</version>
// </dependency>

package com.example.notifications;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class FCMService {

    @PostConstruct
    public void initialize() {
        try {
            FileInputStream serviceAccount = new FileInputStream("path/to/firebase-service-account-key.json");
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendOrderStatusNotification(String userId, String orderId, String newStatus, String fcmToken) {
        try {
            // Create notification
            Notification notification = Notification.builder()
                    .setTitle("Order Status Updated")
                    .setBody(String.format("Your order #%s is now %s", orderId, newStatus))
                    .build();

            // Create data payload
            Map<String, String> data = new HashMap<>();
            data.put("type", "order_status_update");
            data.put("orderId", orderId);
            data.put("status", newStatus);
            data.put("userId", userId);

            // Build message
            Message message = Message.builder()
                    .setNotification(notification)
                    .putAllData(data)
                    .setToken(fcmToken)
                    .build();

            // Send message
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent message: " + response);
            return response;

        } catch (FirebaseMessagingException e) {
            System.err.println("Error sending FCM message: " + e.getMessage());
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    public void updateOrderStatusWithNotification(String orderId, String newStatus) {
        try {
            // 1. Update order in database
            updateOrderInDatabase(orderId, newStatus);
            
            // 2. Get user info and FCM token
            Order order = getOrderById(orderId);
            User user = getUserById(order.getUserId());
            
            if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                // 3. Send push notification
                sendOrderStatusNotification(
                    user.getId(), 
                    orderId, 
                    newStatus, 
                    user.getFcmToken()
                );
            }
            
        } catch (Exception e) {
            System.err.println("Error updating order status: " + e.getMessage());
        }
    }
}
