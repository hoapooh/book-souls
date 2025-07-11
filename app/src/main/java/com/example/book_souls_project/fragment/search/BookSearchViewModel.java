package com.example.book_souls_project.fragment.search;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.api.repository.BookRepository;
import com.example.book_souls_project.api.repository.CategoryRepository;
import com.example.book_souls_project.api.types.book.Book;
import com.example.book_souls_project.api.types.book.BookListResponse;
import com.example.book_souls_project.api.types.category.Category;
import com.example.book_souls_project.api.types.category.CategoryListResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BookSearchViewModel extends AndroidViewModel {
    private static final String TAG = "BookSearchViewModel";
    
    private BookRepository bookRepository;
    private CategoryRepository categoryRepository;
    
    // LiveData for books
    private MutableLiveData<List<Book>> searchResults = new MutableLiveData<>();
    
    // LiveData for categories
    private MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    
    // Selected category IDs
    private Set<String> selectedCategoryIds = new HashSet<>();
    
    // Search query
    private String currentSearchQuery = "";
    
    // LiveData for loading states
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    // LiveData for error messages
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public BookSearchViewModel(@NonNull Application application) {
        super(application);
        bookRepository = ApiRepository.getInstance(application).getBookRepository();
        categoryRepository = ApiRepository.getInstance(application).getCategoryRepository();
    }

    // Getters for LiveData (observed by the Fragment)
    public LiveData<List<Book>> getSearchResults() {
        return searchResults;
    }
    
    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    // Category selection methods
    public void toggleCategorySelection(String categoryId) {
        // Log the change for debugging
        Log.d(TAG, "Toggling category selection: " + categoryId);
        Log.d(TAG, "Before toggle - Selected categories: " + selectedCategoryIds.toString());
        
        if (selectedCategoryIds.contains(categoryId)) {
            selectedCategoryIds.remove(categoryId);
        } else {
            selectedCategoryIds.add(categoryId);
        }
        
        Log.d(TAG, "After toggle - Selected categories: " + selectedCategoryIds.toString());
        
        // Re-run search with updated categories
        if (!currentSearchQuery.isEmpty() || !selectedCategoryIds.isEmpty()) {
            searchBooks(currentSearchQuery);
        }
    }
    
    public boolean isCategorySelected(String categoryId) {
        return selectedCategoryIds.contains(categoryId);
    }
    
    public Set<String> getSelectedCategoryIds() {
        return selectedCategoryIds;
    }
    
    public void clearCategorySelections() {
        selectedCategoryIds.clear();
        if (!currentSearchQuery.isEmpty()) {
            searchBooks(currentSearchQuery);
        } else {
            loadPopularBooks();
        }
    }
    
    // Methods to trigger data loading
    public void loadCategories() {
        categoryRepository.getCategories(new CategoryRepository.CategoryCallback() {
            @Override
            public void onCategoriesLoaded(CategoryListResponse response) {
                categories.postValue(response.getResult().getItems());
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue("Error loading categories: " + error);
            }
            
            @Override
            public void onLoading() {
                // We don't need to show loading for categories
            }
        });
    }
    
    public void loadPopularBooks() {
        isLoading.setValue(true);
        
        bookRepository.getBooks(new BookRepository.BookCallback() {
            @Override
            public void onBooksLoaded(BookListResponse response) {
                searchResults.postValue(response.getResult().getItems());
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
        currentSearchQuery = query;
        
        // Build query parameters map for flexible search
        Map<String, String> queryParams = new HashMap<>();
        
        // Add title if present
        if (query != null && !query.isEmpty()) {
            queryParams.put("Title", query);
        }
        
        // Add selected category IDs
        if (!selectedCategoryIds.isEmpty()) {
            // When using HashMap with Retrofit's @QueryMap, we can't use the same key multiple times
            // We need to handle this using a MultiValueMap or by another approach
            
            // For our simplified BookService, we'll use a workaround by concatenating category IDs
            // and then handling them in the searchBooksWithParams method
            
            int i = 0;
            for (String categoryId : selectedCategoryIds) {
                // Use unique parameter keys for each category ID
                queryParams.put("CategoryIds" + i, categoryId);
                i++;
            }
            
            // Also add the count of categories for reference
            queryParams.put("CategoryCount", String.valueOf(selectedCategoryIds.size()));
        }
        
        // If no search criteria, use empty search to get all books
        // or we could call loadPopularBooks() instead
        
        // Log search parameters
        Log.d(TAG, "Search params: " + queryParams);
        
        // Use the simplified search method
        bookRepository.searchBooksWithParams(queryParams, new BookRepository.BookCallback() {
            @Override
            public void onBooksLoaded(BookListResponse response) {
                searchResults.postValue(response.getResult().getItems());
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