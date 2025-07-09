package com.example.book_souls_project.api.repository;

import android.content.Context;

import com.example.book_souls_project.api.service.BookService;
import com.example.book_souls_project.api.types.book.BookListResponse;

import retrofit2.Call;

public class BookRepository extends BaseRepository {
    private BookService bookService;

    public BookRepository(Context context) {
        super(context);
        this.bookService = apiClient.getRetrofit().create(BookService.class);
    }

    public void getBooks(BookCallback callback) {
        Call<BookListResponse> call = bookService.getBooks();
        
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
        Call<BookListResponse> call = bookService.searchBooks(search);
        
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
        Call<BookListResponse> call = bookService.getBooksByCategory(categoryId);
        
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

    public interface BookCallback {
        void onBooksLoaded(BookListResponse response);
        void onError(String error);
        default void onLoading() {}
    }
}
