package com.example.book_souls_project.api.service;

import com.example.book_souls_project.api.types.notification.CreateNotificationRequest;
import com.example.book_souls_project.api.types.notification.NotificationListResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface NotificationService {
    
    @POST("notifications")
    Call<Void> createNotification(@Body CreateNotificationRequest request);
    
    @GET("notifications")
    Call<NotificationListResponse> getNotifications();
}
