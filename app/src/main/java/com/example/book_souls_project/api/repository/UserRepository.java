package com.example.book_souls_project.api.repository;

import android.content.Context;
import android.util.Log;

import com.example.book_souls_project.api.service.UserService;
import com.example.book_souls_project.api.types.user.EditProfileRequest;
import com.example.book_souls_project.api.types.user.UserAddress;
import com.example.book_souls_project.api.types.user.UserProfile;
import com.example.book_souls_project.util.DataUtils;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class UserRepository extends BaseRepository {
    private UserService userService;

    public UserRepository(Context context) {
        super(context);
        this.userService = apiClient.getRetrofit().create(UserService.class);
    }

    public void getUserProfile(UserProfileCallback callback) {
        String token = tokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            callback.onError("No authentication token found");
            return;
        }

        String authHeader = "Bearer " + token;
        Call<UserProfile> call = userService.getUserProfile(authHeader);

        executeCall(call, new ApiCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                // Clean the data from extra quotes
                DataUtils.cleanUserProfile(userProfile);
                
                // Update token manager with latest user info
                if (userProfile != null) {
                    tokenManager.saveUserSession(
                        token,
                        userProfile.getId(),
                        userProfile.getFullName(),
                        userProfile.getRole(),
                        userProfile.getAvatar()
                    );
                }
                callback.onUserProfileLoaded(userProfile);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }

            @Override
            public void onLoading() {
                callback.onLoading();
            }
        });
    }

    public void editProfile(EditProfileRequest request, UserProfileCallback callback) {
        String token = tokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            callback.onError("No authentication token found");
            return;
        }

        String authHeader = "Bearer " + token;
                Call<UserProfile> call = userService.editProfile(authHeader, request);

        executeCall(call, new ApiCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                // Clean the data from extra quotes
                DataUtils.cleanUserProfile(userProfile);
                
                // Update token manager with updated user info
                if (userProfile != null) {
                    tokenManager.saveUserSession(
                        token,
                        userProfile.getId(),
                        userProfile.getFullName(),
                        userProfile.getRole(),
                        userProfile.getAvatar()
                    );
                }
                callback.onUserProfileUpdated(userProfile);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }

            @Override
            public void onLoading() {
                callback.onLoading();
            }
        });
    }

    public void editProfileWithAvatar(String fullName, String phoneNumber, String gender,
                                     String addressStreet, String addressWard, String addressDistrict,
                                     String addressCity, String addressCountry, 
                                     MultipartBody.Part avatar, UserProfileCallback callback) {
        String token = tokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            callback.onError("No authentication token found");
            return;
        }

        String authHeader = "Bearer " + token;
        
        // Log the address fields being sent
        Log.d("UserRepository", "Updating profile with address fields:");
        Log.d("UserRepository", "Street: " + addressStreet);
        Log.d("UserRepository", "Ward: " + addressWard);
        Log.d("UserRepository", "District: " + addressDistrict);
        Log.d("UserRepository", "City: " + addressCity);
        Log.d("UserRepository", "Country: " + addressCountry);
        Log.d("UserRepository", "Has avatar: " + (avatar != null));
        
        Call<UserProfile> call;
        
        if (avatar != null) {
            Log.d("UserRepository", "Using multipart with URL parameters + avatar");
            // Use multipart with URL parameters for text fields, only avatar in body
            call = userService.editProfileWithAvatar(
                authHeader, 
                fullName, phoneNumber, gender,
                addressStreet, addressWard, addressDistrict, 
                addressCity, addressCountry,
                avatar
            );
        } else {
            Log.d("UserRepository", "Using URL parameters only (no avatar)");
            // Use URL parameters only when there's no avatar (no multipart needed)
            call = userService.editProfileFields(
                authHeader, 
                fullName, phoneNumber, gender,
                addressStreet, addressWard, addressDistrict, 
                addressCity, addressCountry
            );
        }

        executeCall(call, new ApiCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                Log.d("UserRepository", "Profile update successful!");
                if (userProfile != null && userProfile.getAddress() != null) {
                    Log.d("UserRepository", "Updated address - Street: " + userProfile.getAddress().getStreet());
                    Log.d("UserRepository", "Updated address - Ward: " + userProfile.getAddress().getWard());
                    Log.d("UserRepository", "Updated address - District: " + userProfile.getAddress().getDistrict());
                    Log.d("UserRepository", "Updated address - City: " + userProfile.getAddress().getCity());
                    Log.d("UserRepository", "Updated address - Country: " + userProfile.getAddress().getCountry());
                } else {
                    Log.w("UserRepository", "Address is null in response");
                }
                
                // Clean the data from extra quotes
                DataUtils.cleanUserProfile(userProfile);
                
                // Update token manager with updated user info
                if (userProfile != null) {
                    tokenManager.saveUserSession(
                        token,
                        userProfile.getId(),
                        userProfile.getFullName(),
                        userProfile.getRole(),
                        userProfile.getAvatar()
                    );
                }
                callback.onUserProfileUpdated(userProfile);
            }

            @Override
            public void onError(String error) {
                Log.e("UserRepository", "Profile update failed: " + error);
                callback.onError(error);
            }

            @Override
            public void onLoading() {
                callback.onLoading();
            }
        });
    }

    public interface UserProfileCallback {
        void onUserProfileLoaded(UserProfile userProfile);
        void onUserProfileUpdated(UserProfile userProfile);
        void onError(String error);
        default void onLoading() {}
    }
}
