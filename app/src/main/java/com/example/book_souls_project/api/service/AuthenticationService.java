package com.example.book_souls_project.api.service;

import com.example.book_souls_project.api.types.auth.LoginRequest;
import com.example.book_souls_project.api.types.auth.LoginResponse;
import com.example.book_souls_project.api.types.auth.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthenticationService {
    String AUTHENTICATION = "authentication";

    @POST(AUTHENTICATION + "/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST(AUTHENTICATION + "/register")
    Call<Void> register(@Body RegisterRequest request);
}
