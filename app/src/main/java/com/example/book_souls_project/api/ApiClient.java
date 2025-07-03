package com.example.book_souls_project.api;

import android.content.Context;
import android.util.Log;

import com.example.book_souls_project.api.interceptor.AuthInterceptor;
import com.example.book_souls_project.util.Constants;
import com.example.book_souls_project.util.TokenManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static ApiClient instance;

    private Retrofit retrofit;
    private TokenManager tokenManager;
    private String baseUrl;

    private ApiClient(Context context) {
        this.tokenManager = new TokenManager(context);
        this.baseUrl = Constants.Api.getBaseUrl(context);
        initializeRetrofit();
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
        return instance;
    }

    private void initializeRetrofit() {
        // Create logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
            Log.d(TAG, message);
        });
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create Auth interceptor
        AuthInterceptor authInterceptor = new AuthInterceptor(tokenManager);

        // Create OkHttpClient with timeouts and interceptors
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    // Backward compatibility
    @Deprecated
    public static Retrofit getClient() {
        throw new UnsupportedOperationException("Use getInstance(context).getRetrofit() instead");
    }
}
