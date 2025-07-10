package com.example.book_souls_project.api.repository;

import android.content.Context;
import android.util.Log;

import com.example.book_souls_project.api.ApiClient;
import com.example.book_souls_project.util.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseRepository {
    protected ApiClient apiClient;
    protected TokenManager tokenManager;

    public BaseRepository(Context context) {
        this.apiClient = ApiClient.getInstance(context);
        this.tokenManager = apiClient.getTokenManager();
    }

    // Generic callback interface
    public interface ApiCallback<T> {
        void onSuccess(T data);
        void onError(String error);
        default void onLoading() {}
    }

    // Generic error handler
    protected <T> void executeCall(Call<T> call, ApiCallback<T> callback) {
        callback.onLoading();
        
        Log.d("BaseRepository", "Making API call to: " + call.request().url());
        
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                Log.d("BaseRepository", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("BaseRepository", "API call successful");
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = getErrorMessage(response);
                    Log.e("BaseRepository", "API call failed: " + errorMessage);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                Log.e("BaseRepository", "Network error: " + t.getMessage(), t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private <T> String getErrorMessage(Response<T> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e("BaseRepository", "Error body: " + errorBody);
            }
        } catch (Exception e) {
            Log.e("BaseRepository", "Error parsing error body", e);
        }
        
        switch (response.code()) {
            case 400:
                return "Bad request - Please check your input";
            case 401:
                return "Unauthorized - Please login again";
            case 403:
                return "Forbidden - You don't have permission";
            case 404:
                return "Not found - Resource doesn't exist";
            case 500:
                return "Server error - Please try again later";
            default:
                return "Error: " + response.message();
        }
    }
}
