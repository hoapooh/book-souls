package com.example.book_souls_project.api;

public class ApiRepository {
    public static ApiService getRandomService() {
        return ApiClient.getClient().create(ApiService.class);
    }
}
