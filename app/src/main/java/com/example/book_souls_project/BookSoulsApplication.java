package com.example.book_souls_project;

import android.app.Application;
import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.util.TokenManager;

public class BookSoulsApplication extends Application {
    
    private static BookSoulsApplication instance;
    private ApiRepository apiRepository;
    private TokenManager tokenManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Initialize global components
        apiRepository = ApiRepository.getInstance(this);
        tokenManager = new TokenManager(this);
    }
    
    public static BookSoulsApplication getInstance() {
        return instance;
    }
    
    public ApiRepository getApiRepository() {
        return apiRepository;
    }
    
    public TokenManager getTokenManager() {
        return tokenManager;
    }
    
    /**
     * Quick access to check login status from anywhere in the app
     */
    public boolean isUserLoggedIn() {
        return apiRepository.getAuthRepository().isLoggedIn();
    }
}
