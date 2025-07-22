package com.example.book_souls_project.api.service;

import com.example.book_souls_project.api.types.user.EditProfileRequest;
import com.example.book_souls_project.api.types.user.UserProfile;
import com.example.book_souls_project.api.types.user.FCMTokenRequest;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface UserService {
    String USERS = "users";

    @GET(USERS + "/profile")
    Call<UserProfile> getUserProfile(@Header("Authorization") String authorization);

    // Method without Authorization header - AuthInterceptor will add it automatically
    @GET(USERS + "/profile")
    Call<UserProfile> getUserProfile();

    // Use separate endpoints to avoid the update operator issue
    @PUT(USERS + "/edit-profile")
    Call<UserProfile> editProfile(@Header("Authorization") String authorization,
                                  @Body EditProfileRequest request);

    @Multipart
    @PUT(USERS + "/edit-profile")
    Call<UserProfile> editProfileWithAvatar(@Header("Authorization") String authorization,
                                           @Query("FullName") String fullName,
                                           @Query("PhoneNumber") String phoneNumber,
                                           @Query("Gender") String gender,
                                           @Query("Address.Street") String addressStreet,
                                           @Query("Address.Ward") String addressWard,
                                           @Query("Address.District") String addressDistrict,
                                           @Query("Address.City") String addressCity,
                                           @Query("Address.Country") String addressCountry,
                                           @Part MultipartBody.Part avatar);

    // Alternative endpoint for profile updates without avatar
    @PUT(USERS + "/edit-profile")
    Call<UserProfile> editProfileFields(@Header("Authorization") String authorization,
                                       @Query("FullName") String fullName,
                                       @Query("PhoneNumber") String phoneNumber,
                                       @Query("Gender") String gender,
                                       @Query("Address.Street") String addressStreet,
                                       @Query("Address.Ward") String addressWard,
                                       @Query("Address.District") String addressDistrict,
                                       @Query("Address.City") String addressCity,
                                       @Query("Address.Country") String addressCountry);

    // FCM Token management
    @POST(USERS + "/fcm-token")
    Call<Void> updateFCMToken(@Header("Authorization") String authorization,
                              @Body FCMTokenRequest request);

    @PUT(USERS + "/fcm-token")
    Call<Void> refreshFCMToken(@Header("Authorization") String authorization,
                               @Body FCMTokenRequest request);
}
