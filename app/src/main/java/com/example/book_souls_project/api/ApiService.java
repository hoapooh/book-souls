package com.example.book_souls_project.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    String RANDOM = "random";

    @GET(RANDOM)
    Call<String> getRandom();
}
