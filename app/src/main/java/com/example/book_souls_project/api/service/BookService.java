package com.example.book_souls_project.api.service;

import com.example.book_souls_project.api.types.book.BookDetailResponse;
import com.example.book_souls_project.api.types.book.BookListResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface BookService {
    String BOOKS = "books";

    // Get all books without pagination
    @GET(BOOKS)
    Call<BookListResponse> getBooks();

    // Get book by ID
    @GET(BOOKS + "/{id}")
    Call<BookDetailResponse> getBookById(@Path("id") String bookId);

    // Simple search using 'search' parameter (for backward compatibility)
    @GET(BOOKS)
    Call<BookListResponse> searchBooks(@Query("search") String search);

    // Flexible search that supports any combination of query parameters
    @GET(BOOKS)
    Call<BookListResponse> searchBooksWithParams(@QueryMap Map<String, String> queryParams);
    
    // Advanced search that supports any query parameters with complete URL
    @GET
    Call<BookListResponse> searchBooksAdvanced(@Url String url);
}
