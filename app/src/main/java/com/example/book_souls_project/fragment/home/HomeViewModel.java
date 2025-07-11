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

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    
    private BookRepository bookRepository;
    
    // LiveData for books
    private MutableLiveData<List<Book>> featuredBooks = new MutableLiveData<>();
    private MutableLiveData<List<Book>> recentBooks = new MutableLiveData<>();
    
    // LiveData for loading states
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoadingMore = new MutableLiveData<>();
    
    // LiveData for error messages
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Pagination tracking
    private int currentPageIndex = 1;
    private int limit = 10;
    private boolean hasMoreData = true;
    
    // Lists to accumulate books from different pages
    private List<Book> allFeaturedBooks = new ArrayList<>();
    private List<Book> allRecentBooks = new ArrayList<>();

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

    public LiveData<Boolean> getIsLoadingMore() {
        return isLoadingMore;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public boolean hasMoreData() {
        return hasMoreData;
    }

    public int getLimit() {
        return limit;
    }

    // Methods to trigger data loading
    public void loadBooks() {
        // Reset pagination state for fresh load
        currentPageIndex = 1;
        hasMoreData = true;
        allFeaturedBooks.clear();
        allRecentBooks.clear();
        
        isLoading.setValue(true);
        isLoadingMore.setValue(false);
        
        bookRepository.getBooks(currentPageIndex, limit, new BookRepository.BookCallback() {
            @Override
            public void onBooksLoaded(BookListResponse response) {
                List<Book> books = response.getResult().getItems();
                
                // Store books
                allFeaturedBooks.addAll(books);
                
                // Update LiveData on main thread
                featuredBooks.postValue(new ArrayList<>(allFeaturedBooks));
                
                // Set recent books (first 5 books)
                if (books.size() > 0) {
                    List<Book> recent = books.size() > 5 ? books.subList(0, 5) : books;
                    allRecentBooks.addAll(recent);
                    recentBooks.postValue(new ArrayList<>(allRecentBooks));
                }
                
                // Check if there might be more data
                hasMoreData = books.size() >= limit;
                
                isLoading.postValue(false);
                isLoadingMore.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
                isLoadingMore.postValue(false);
            }

            @Override
            public void onLoading() {
                isLoading.postValue(true);
            }
        });
    }
    
    public void loadMoreBooks() {
        if (!hasMoreData || isLoadingMore.getValue() == Boolean.TRUE) {
            return;
        }
        
        isLoadingMore.setValue(true);
        currentPageIndex++;
        
        bookRepository.getBooks(currentPageIndex, limit, new BookRepository.BookCallback() {
            @Override
            public void onBooksLoaded(BookListResponse response) {
                List<Book> books = response.getResult().getItems();
                
                if (books.isEmpty()) {
                    // No more data available
                    hasMoreData = false;
                    isLoadingMore.postValue(false);
                    return;
                }
                
                // Add newly loaded books to our collections
                allFeaturedBooks.addAll(books);
                
                // Update LiveData
                featuredBooks.postValue(new ArrayList<>(allFeaturedBooks));
                
                // Check if there might be more data
                hasMoreData = books.size() >= limit;
                
                isLoadingMore.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoadingMore.postValue(false);
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