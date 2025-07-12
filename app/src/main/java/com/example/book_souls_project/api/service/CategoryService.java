package com.example.book_souls_project.api.service;

import com.example.book_souls_project.api.types.category.CategoryDetailResponse;
import com.example.book_souls_project.api.types.category.CategoryListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CategoryService {
    
    @GET("categories")
    Call<CategoryListResponse> getCategories();
    
    @GET("categories/{id}")
    Call<CategoryDetailResponse> getCategoryById(@Path("id") String categoryId);
}
