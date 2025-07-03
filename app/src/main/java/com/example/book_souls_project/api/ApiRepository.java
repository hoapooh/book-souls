package com.example.book_souls_project.api;

import android.content.Context;

import com.example.book_souls_project.api.repository.AuthRepository;
import com.example.book_souls_project.api.service.AuthenticationService;

/**
 * Central API Repository Factory
 * Provides instances of specific repositories
 */
public class ApiRepository {
    private static ApiRepository instance;
    private Context context;
    
    // Repository instances
    private AuthRepository authRepository;

    private ApiRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized ApiRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ApiRepository(context);
        }
        return instance;
    }

    // Get AuthRepository instance
    public AuthRepository getAuthRepository() {
        if (authRepository == null) {
            authRepository = new AuthRepository(context);
        }
        return authRepository;
    }

    // Deprecated methods for backward compatibility
    @Deprecated
    public static ApiService getRandomService() {
        throw new UnsupportedOperationException("Use getInstance(context) pattern instead");
    }

    @Deprecated
    public static AuthenticationService getAuthenticationService() {
        throw new UnsupportedOperationException("Use getInstance(context).getAuthRepository() instead");
    }
}
