package com.example.book_souls_project.api.repository;

import android.content.Context;

import com.example.book_souls_project.api.service.CategoryService;
import com.example.book_souls_project.api.types.category.Category;
import com.example.book_souls_project.api.types.category.CategoryDetailResponse;
import com.example.book_souls_project.api.types.category.CategoryListResponse;

import retrofit2.Call;

public class CategoryRepository extends BaseRepository {
    private CategoryService categoryService;

    public CategoryRepository(Context context) {
        super(context);
        this.categoryService = apiClient.getRetrofit().create(CategoryService.class);
    }

    public void getCategories(CategoryCallback callback) {
        Call<CategoryListResponse> call = categoryService.getCategories();
        
        executeCall(call, new ApiCallback<CategoryListResponse>() {
            @Override
            public void onSuccess(CategoryListResponse response) {
                callback.onCategoriesLoaded(response);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }

            @Override
            public void onLoading() {
                callback.onLoading();
            }
        });
    }

    public void getCategoryById(String categoryId, CategoryDetailCallback callback) {
        Call<CategoryDetailResponse> call = categoryService.getCategoryById(categoryId);
        
        executeCall(call, new ApiCallback<CategoryDetailResponse>() {
            @Override
            public void onSuccess(CategoryDetailResponse response) {
                callback.onCategoryLoaded(response.getResult());
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }

            @Override
            public void onLoading() {
                callback.onLoading();
            }
        });
    }

    public interface CategoryCallback {
        void onCategoriesLoaded(CategoryListResponse response);
        void onError(String error);
        default void onLoading() {}
    }
    
    public interface CategoryDetailCallback {
        void onCategoryLoaded(Category category);
        void onError(String error);
        default void onLoading() {}
    }
}
