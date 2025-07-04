package com.example.book_souls_project.fragment.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.api.repository.BookRepository;
import com.example.book_souls_project.api.types.book.Book;
import com.example.book_souls_project.api.types.book.BookListResponse;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    
    private BookRepository bookRepository;
    
    // LiveData for books
    private MutableLiveData<List<Book>> featuredBooks = new MutableLiveData<>();
    private MutableLiveData<List<Book>> recentBooks = new MutableLiveData<>();
    
    // LiveData for loading states
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    
    // LiveData for error messages
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        bookRepository = ApiRepository.getInstance(application).getBookRepository();
    }

    // Getters for LiveData (observed by the Fragment)
    public LiveData<List<Book>> getFeaturedBooks() {
        return featuredBooks;
    }

    public LiveData<List<Book>> getRecentBooks() {
        return recentBooks;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Methods to trigger data loading
    public void loadBooks() {
        isLoading.setValue(true);
        
        bookRepository.getBooks(new BookRepository.BookCallback() {
            @Override
            public void onBooksLoaded(BookListResponse response) {
                List<Book> books = response.getResult().getItems();
                
                // Update LiveData on main thread
                featuredBooks.postValue(books);
                
                // Set recent books (first 5 books)
                List<Book> recent = books.size() > 5 ? books.subList(0, 5) : books;
                recentBooks.postValue(recent);
                
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }

            @Override
            public void onLoading() {
                isLoading.postValue(true);
            }
        });
    }

    public void searchBooks(String query) {
        isLoading.setValue(true);
        
        bookRepository.searchBooks(query, new BookRepository.BookCallback() {
            @Override
            public void onBooksLoaded(BookListResponse response) {
                List<Book> books = response.getResult().getItems();
                featuredBooks.postValue(books);
                recentBooks.postValue(books);
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}