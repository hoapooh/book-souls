package com.example.book_souls_project.api.service;

import com.example.book_souls_project.api.types.book.BookListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BookService {
    String BOOKS = "books";

    // Get all books without pagination
    @GET(BOOKS)
    Call<BookListResponse> getBooks();

    // Search books with optional search parameter
    @GET(BOOKS)
    Call<BookListResponse> searchBooks(@Query("search") String search);

    // Get books by category
    @GET(BOOKS)
    Call<BookListResponse> getBooksByCategory(@Query("categoryId") String categoryId);
}
