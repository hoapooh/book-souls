package com.example.book_souls_project.api.repository;

import android.content.Context;

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
        
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = getErrorMessage(response);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private <T> String getErrorMessage(Response<T> response) {
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
