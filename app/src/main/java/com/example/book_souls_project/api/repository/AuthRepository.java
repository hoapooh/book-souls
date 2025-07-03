package com.example.book_souls_project.api.repository;

import android.content.Context;

import com.example.book_souls_project.api.service.AuthenticationService;
import com.example.book_souls_project.api.types.auth.LoginRequest;
import com.example.book_souls_project.api.types.auth.LoginResponse;
import com.example.book_souls_project.api.types.auth.LoginResponseToken;

import retrofit2.Call;

public class AuthRepository extends BaseRepository {
    private AuthenticationService authService;

    public AuthRepository(Context context) {
        super(context);
        this.authService = apiClient.getRetrofit().create(AuthenticationService.class);
    }

    public void login(String email, String password, AuthCallback callback) {
        LoginRequest request = new LoginRequest(email, password);
        Call<LoginResponse> call = authService.login(request);
        
        executeCall(call, new ApiCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                if (response.getToken() != null) {
                    // Save user session
                    LoginResponseToken token = response.getToken();
                    tokenManager.saveUserSession(
                        token.getAccessToken(),
                        token.getId(),
                        token.getFullName(),
                        token.getRole(),
                        token.getAvatar()
                    );
                    callback.onLoginSuccess(response);
                } else {
                    callback.onLoginError("Invalid response from server");
                }
            }

            @Override
            public void onError(String error) {
                callback.onLoginError(error);
            }

            @Override
            public void onLoading() {
                callback.onLoginLoading();
            }
        });
    }

    public void logout() {
        tokenManager.clearSession();
    }

    public boolean isLoggedIn() {
        return tokenManager.isLoggedIn();
    }

    public interface AuthCallback {
        void onLoginSuccess(LoginResponse response);
        void onLoginError(String error);
        default void onLoginLoading() {}
    }
}
