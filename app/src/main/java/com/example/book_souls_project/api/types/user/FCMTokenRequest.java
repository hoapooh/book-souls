package com.example.book_souls_project.api.types.user;

public class FCMTokenRequest {
    private String userId;
    private String fcmToken;
    private String deviceId; // Optional: to handle multiple devices
    
    public FCMTokenRequest() {}
    
    public FCMTokenRequest(String userId, String fcmToken) {
        this.userId = userId;
        this.fcmToken = fcmToken;
    }
    
    public FCMTokenRequest(String userId, String fcmToken, String deviceId) {
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.deviceId = deviceId;
    }
    
    // Getters and setters
    public String getUserId() { 
        return userId; 
    }
    
    public void setUserId(String userId) { 
        this.userId = userId; 
    }
    
    public String getFcmToken() { 
        return fcmToken; 
    }
    
    public void setFcmToken(String fcmToken) { 
        this.fcmToken = fcmToken; 
    }
    
    public String getDeviceId() { 
        return deviceId; 
    }
    
    public void setDeviceId(String deviceId) { 
        this.deviceId = deviceId; 
    }
}
