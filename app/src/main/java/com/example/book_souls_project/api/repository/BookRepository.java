package com.example.book_souls_project.api.repository;

import android.content.Context;


import com.example.book_souls_project.api.service.BookService;
import com.example.book_souls_project.api.types.book.Book;
import com.example.book_souls_project.api.types.book.BookDetailResponse;
import com.example.book_souls_project.api.types.book.BookListResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

public class BookRepository extends BaseRepository {
    private BookService bookService;

    public BookRepository(Context context) {
        super(context);
        this.bookService = apiClient.getRetrofit().create(BookService.class);
    }

    public void getBooks(int pageIndex, int limit, BookCallback callback) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("pageIndex", String.valueOf(pageIndex));
        queryParams.put("limit", String.valueOf(limit));
        
        Call<BookListResponse> call = bookService.searchBooksWithParams(queryParams);
        
        executeCall(call, new ApiCallback<BookListResponse>() {
            @Override
            public void onSuccess(BookListResponse response) {
                callback.onBooksLoaded(response);
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

    public void getBooks(BookCallback callback) {
        // Default to first page with default limit
        getBooks(1, 10, callback);
    }

    public void searchBooks(String search, int pageIndex, int limit, BookCallback callback) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("search", search);
        queryParams.put("pageIndex", String.valueOf(pageIndex));
        queryParams.put("limit", String.valueOf(limit));
        
        Call<BookListResponse> call = bookService.searchBooksWithParams(queryParams);
        
        executeCall(call, new ApiCallback<BookListResponse>() {
            @Override
            public void onSuccess(BookListResponse response) {
                callback.onBooksLoaded(response);
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

    public void searchBooks(String search, BookCallback callback) {
        // Default to first page with default limit
        searchBooks(search, 1, 10, callback);
    }
    
    public void searchBooksWithParams(Map<String, String> queryParams, BookCallback callback) {
        // Check if we need to process multiple categories
        if (queryParams.containsKey("CategoryCount")) {
            // Get the number of categories
            int categoryCount = Integer.parseInt(queryParams.get("CategoryCount"));
            
            // Create a new map without our custom category keys
            Map<String, String> apiQueryParams = new HashMap<>();
            
            // Copy non-category parameters
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!entry.getKey().startsWith("CategoryIds") && !entry.getKey().equals("CategoryCount")) {
                    apiQueryParams.put(entry.getKey(), entry.getValue());
                }
            }
            
            // Special handling for multiple categories
            if (categoryCount > 0) {
                // For multiple categories, we need to build a URL with repeated parameters
                StringBuilder urlBuilder = new StringBuilder("books?");
                
                // Add other params first
                for (Map.Entry<String, String> entry : apiQueryParams.entrySet()) {
                    urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                
                // Add CategoryIds multiple times (one for each category)
                for (int i = 0; i < categoryCount; i++) {
                    String categoryId = queryParams.get("CategoryIds" + i);
                    urlBuilder.append("CategoryIds=").append(categoryId);
                    if (i < categoryCount - 1) {
                        urlBuilder.append("&");
                    }
                }
                
                // Use the advanced search with our built URL
                String url = urlBuilder.toString();
                
                Call<BookListResponse> call = bookService.searchBooksAdvanced(url);
                
                executeCall(call, new ApiCallback<BookListResponse>() {
                    @Override
                    public void onSuccess(BookListResponse response) {
                        callback.onBooksLoaded(response);
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
                return;
            }
        }
        
        // If no special handling needed, just use the standard QueryMap API
        Call<BookListResponse> call = bookService.searchBooksWithParams(queryParams);
        executeCall(call, new ApiCallback<BookListResponse>() {
            @Override
            public void onSuccess(BookListResponse response) {
                callback.onBooksLoaded(response);
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

    public void getBooksByCategory(String categoryId, BookCallback callback) {
        // Use the more flexible searchBooksWithParams method
        Map<String, String> params = new HashMap<>();
        params.put("categoryId", categoryId);
        Call<BookListResponse> call = bookService.searchBooksWithParams(params);
        
        executeCall(call, new ApiCallback<BookListResponse>() {
            @Override
            public void onSuccess(BookListResponse response) {
                callback.onBooksLoaded(response);
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

    public void getBookById(String bookId, BookDetailCallback callback) {
        Call<BookDetailResponse> call = bookService.getBookById(bookId);
        
        executeCall(call, new ApiCallback<BookDetailResponse>() {
            @Override
            public void onSuccess(BookDetailResponse response) {
                callback.onBookLoaded(response.getResult());
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

    public interface BookCallback {
        void onBooksLoaded(BookListResponse response);
        void onError(String error);
        default void onLoading() {}
    }
    
    public interface BookDetailCallback {
        void onBookLoaded(Book book);
        void onError(String error);
        default void onLoading() {}
    }
}
