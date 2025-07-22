package com.example.book_souls_project.api.types.user;

public class FCMTokenRequest {
    private String fcmToken;
    public FCMTokenRequest() {}
    
    public FCMTokenRequest(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    
    // Getters and setters
    
    public String getFcmToken() { 
        return fcmToken; 
    }
    
    public void setFcmToken(String fcmToken) { 
        this.fcmToken = fcmToken; 
    }
}
