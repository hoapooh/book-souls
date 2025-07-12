package com.example.book_souls_project.fragment.search;

import androidx.lifecycle.ViewModel;
import android.app.Application;

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
    private MutableLiveData<Boolean> isLoadingMore = new MutableLiveData<>(false);
    
    // LiveData for error messages
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    // Pagination tracking
    private int currentPageIndex = 1;
    private int limit = 10;
    private boolean hasMoreData = true;
    
    // Lists to accumulate books from different pages
    private List<Book> allSearchResults = new ArrayList<>();

    // String to hold the initial category ID from navigation
    private String initialCategoryId = null;

    // Flag to track if the ViewModel has been initialized
    private boolean initialized = false;

    // Sort order enum
    public enum SortOrder {
        NONE,       // Default, no sorting
        TITLE_ASC,  // A to Z
        TITLE_DESC  // Z to A
    }
    
    // Current sort order
    private SortOrder currentSortOrder = SortOrder.NONE;
    private MutableLiveData<SortOrder> sortOrder = new MutableLiveData<>(SortOrder.NONE);
    
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
    
    public String getCurrentSearchQuery() {
        return currentSearchQuery;
    }
    
    /**
     * Get the current sort order LiveData
     * @return LiveData of the current sort order
     */
    public LiveData<SortOrder> getSortOrder() {
        return sortOrder;
    }
    
    // Category selection methods
    public void toggleCategorySelection(String categoryId) {
        if (selectedCategoryIds.contains(categoryId)) {
            selectedCategoryIds.remove(categoryId);
        } else {
            selectedCategoryIds.add(categoryId);
        }
        
        // Re-run search with updated categories
        if (!currentSearchQuery.isEmpty() || !selectedCategoryIds.isEmpty()) {
            searchBooks(currentSearchQuery);
        }
    }
    
    public boolean isCategorySelected(String categoryId) {
        return selectedCategoryIds.contains(categoryId);
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
                List<Category> categoryList = response.getResult().getItems();
                categories.postValue(categoryList);
                
                // Check if we need to select an initial category
                if (initialCategoryId != null && !initialCategoryId.isEmpty() && selectedCategoryIds.isEmpty()) {
                    // Only set initial category if no other category has been manually selected
                    selectedCategoryIds.add(initialCategoryId);
                    
                    // Trigger a search with the selected category
                    searchBooks("");
                    
                    // Keep the initialCategoryId to help identify navigation source later
                }
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
        // Reset pagination state for fresh load
        currentPageIndex = 1;
        hasMoreData = true;
        allSearchResults.clear();
        
        isLoading.setValue(true);
        isLoadingMore.setValue(false);
        
        // Mark ViewModel as initialized when popular books are loaded
        setInitialized();
        
        bookRepository.getBooks(currentPageIndex, limit, new BookRepository.BookCallback() {
            @Override
            public void onBooksLoaded(BookListResponse response) {
                List<Book> books = response.getResult().getItems();
                
                // Store books
                allSearchResults.addAll(books);
                
                // Apply sorting and update LiveData
                applySorting();
                
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
    
    public void loadMorePopularBooks() {
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
                
                // Add newly loaded books to our collection
                allSearchResults.addAll(books);
                
                // Apply sorting and update LiveData
                applySorting();
                
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
        // Reset pagination state for fresh search
        currentPageIndex = 1;
        hasMoreData = true;
        allSearchResults.clear();
        
        isLoading.setValue(true);
        isLoadingMore.setValue(false);
        currentSearchQuery = query;
        
        // Mark ViewModel as initialized when search is performed
        setInitialized();
        
        // Build query parameters map for flexible search
        Map<String, String> queryParams = new HashMap<>();
        
        // Add pagination parameters
        queryParams.put("pageIndex", String.valueOf(currentPageIndex));
        queryParams.put("limit", String.valueOf(limit));
        
        // Add title if present
        if (query != null && !query.isEmpty()) {
            queryParams.put("Title", query);
        }
        
        // Add selected category IDs
        if (!selectedCategoryIds.isEmpty()) {
            // If multiple categories are selected, we need to add them as separate parameters
            
            int i = 0;
            for (String categoryId : selectedCategoryIds) {
                // Use unique parameter keys for each category ID
                queryParams.put("CategoryIds" + i, categoryId);
                i++;
            }
            
            // Also add the count of categories for reference
            queryParams.put("CategoryCount", String.valueOf(selectedCategoryIds.size()));
        }
        
        // Use the simplified search method
        bookRepository.searchBooksWithParams(queryParams, new BookRepository.BookCallback() {
            @Override
            public void onBooksLoaded(BookListResponse response) {
                List<Book> books = response.getResult().getItems();
                
                // Store books
                allSearchResults.addAll(books);
                
                // Apply sorting and update LiveData
                applySorting();
                
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
        });
    }

    public void loadMoreBooks() {
        if (!hasMoreData || isLoadingMore.getValue() != null && isLoadingMore.getValue()) {
            return; // No more data to load or already loading
        }
        
        isLoadingMore.setValue(true);
        currentPageIndex++;
        
        // Build query parameters map for the next page
        Map<String, String> queryParams = new HashMap<>();
        
        // Add pagination parameters
        queryParams.put("pageIndex", String.valueOf(currentPageIndex));
        queryParams.put("limit", String.valueOf(limit));
        
        // Add title if present
        if (currentSearchQuery != null && !currentSearchQuery.isEmpty()) {
            queryParams.put("Title", currentSearchQuery);
        }
        
        // Add selected category IDs
        if (!selectedCategoryIds.isEmpty()) {
            int i = 0;
            for (String categoryId : selectedCategoryIds) {
                queryParams.put("CategoryIds" + i, categoryId);
                i++;
            }
            queryParams.put("CategoryCount", String.valueOf(selectedCategoryIds.size()));
        }
        
        // Load more books with the same parameters but next page
        bookRepository.searchBooksWithParams(queryParams, new BookRepository.BookCallback() {
            @Override
            public void onBooksLoaded(BookListResponse response) {
                List<Book> books = response.getResult().getItems();
                
                // Check if we received any books
                if (books.isEmpty()) {
                    // No more data to load
                    hasMoreData = false;
                } else {
                    // Add new books to the existing list
                    allSearchResults.addAll(books);
                    applySorting();
                    
                    // Check if there might be more data
                    hasMoreData = books.size() >= limit;
                }
                
                isLoadingMore.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoadingMore.postValue(false);
            }
        });
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    /**
     * Sets a category to be selected, handling both initial load and subsequent navigation
     * 
     * @param categoryId The category ID to select
     */
    public void setInitialCategoryId(String categoryId) {
        // Check if this is a new navigation with different category ID 
        // or if we're returning from detail view with the same initial category
        boolean isNewNavigation = initialCategoryId == null || !initialCategoryId.equals(categoryId);
        
        // Update the initial category ID
        this.initialCategoryId = categoryId;
        
        // Only reset and clear selections if this is truly a new navigation from home screen,
        // not a return from detail view with the same initial category ID
        if (isNewNavigation) {
            // Reset previous search query when selecting a category from navigation
            this.currentSearchQuery = "";
            
            // Reset pagination state for fresh load
            currentPageIndex = 1;
            hasMoreData = true;
            allSearchResults.clear();
            
            // If categories are already loaded, select this category immediately
            List<Category> loadedCategories = categories.getValue();
            if (loadedCategories != null && !loadedCategories.isEmpty()) {
                // Add this category to selected IDs, replacing any previous selections
                selectedCategoryIds.clear();
                selectedCategoryIds.add(categoryId);
                
                // Search with the selected category
                searchBooks("");
            }
        }
        
        // Mark ViewModel as initialized when setting category
        setInitialized();
    }

    /**
     * Checks if any category is currently selected
     * @return true if at least one category is selected
     */
    public boolean hasAnyCategorySelected() {
        return !selectedCategoryIds.isEmpty();
    }
    
    /**
     * Checks if this ViewModel has been initialized
     * @return true if already initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Marks this ViewModel as initialized
     */
    public void setInitialized() {
        this.initialized = true;
    }

    /**
     * Toggle between sort orders: NONE -> TITLE_ASC -> TITLE_DESC -> NONE
     */
    public void toggleSortOrder() {
        switch (currentSortOrder) {
            case NONE:
                currentSortOrder = SortOrder.TITLE_ASC;
                break;
            case TITLE_ASC:
                currentSortOrder = SortOrder.TITLE_DESC;
                break;
            case TITLE_DESC:
            default:
                currentSortOrder = SortOrder.NONE;
                break;
        }
        
        sortOrder.setValue(currentSortOrder);
        
        // Apply sorting to current results
        applySorting();
    }
    
    /**
     * Apply current sort order to search results
     */
    private void applySorting() {
        if (allSearchResults.isEmpty()) {
            return;
        }
        
        List<Book> sortedList = new ArrayList<>(allSearchResults);
        
        switch (currentSortOrder) {
            case TITLE_ASC:
                // Sort by title ascending (A to Z)
                sortedList.sort((book1, book2) -> {
                    if (book1.getTitle() == null) return -1;
                    if (book2.getTitle() == null) return 1;
                    return book1.getTitle().compareToIgnoreCase(book2.getTitle());
                });
                break;
                
            case TITLE_DESC:
                // Sort by title descending (Z to A)
                sortedList.sort((book1, book2) -> {
                    if (book1.getTitle() == null) return 1;
                    if (book2.getTitle() == null) return -1;
                    return book2.getTitle().compareToIgnoreCase(book1.getTitle());
                });
                break;
                
            case NONE:
            default:
                // No sorting, leave as is
                break;
        }
        
        // Update the LiveData with sorted results
        searchResults.postValue(sortedList);
    }
}