package com.example.book_souls_project.fragment.search;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.example.book_souls_project.R;
import com.example.book_souls_project.adapter.BookSearchAdapter;
import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.api.repository.CategoryRepository;
import com.example.book_souls_project.api.types.book.Book;
import com.example.book_souls_project.api.types.category.Category;
import com.example.book_souls_project.util.CartManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BookSearchFragment extends Fragment {

    private static final String TAG = "BookSearchFragment";
    
    private BookSearchViewModel viewModel;
    private BookSearchAdapter searchAdapter;
    private CartManager cartManager;
    
    // UI components
    private RecyclerView recyclerViewSearchResults;
    private EditText editTextSearch;
    private ImageButton buttonFilter;
    private ChipGroup chipGroup;
    private Chip chipAll;
    private TextView textResults;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    
    // Map to keep track of category chips by ID
    private Map<String, Chip> categoryChips = new HashMap<>();
    
    // Handler for search delay
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_search, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(BookSearchViewModel.class);
        
        // Initialize CartManager
        cartManager = CartManager.getInstance(requireContext());
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup click listeners
        setupClickListeners();
        
        // Observe ViewModel LiveData
        observeViewModel();
        
        // Load data
        viewModel.loadCategories();
        viewModel.loadPopularBooks();
    }
    
    private void initViews(View view) {
        recyclerViewSearchResults = view.findViewById(R.id.recyclerViewSearchResults);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        buttonFilter = view.findViewById(R.id.buttonFilter);
        chipGroup = view.findViewById(R.id.chipGroup);
        chipAll = view.findViewById(R.id.chipAll);
        textResults = view.findViewById(R.id.textResults);
        emptyState = view.findViewById(R.id.emptyState);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Make sure the All chip has a simple appearance that works
        chipAll.setTextColor(Color.WHITE);
        chipAll.setChipBackgroundColorResource(R.color.primary_color);
        
        // Initially set results text
        textResults.setText("Popular Books");
    }
    
    private void setupRecyclerView() {
        searchAdapter = new BookSearchAdapter();
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSearchResults.setAdapter(searchAdapter);
        
        // Set cart manager for adapter
        searchAdapter.setCartManager(cartManager);
        
        // Set category repository for fetching category names
        CategoryRepository categoryRepository = ApiRepository.getInstance(requireContext()).getCategoryRepository();
        searchAdapter.setCategoryRepository(categoryRepository);
        
        // Set click listeners for book items
        searchAdapter.setOnBookClickListener(this::onBookClick);
        searchAdapter.setOnAddToCartClickListener(this::onAddToCartClick);
    }
    
    private void setupClickListeners() {
        // Search input listener with debounce
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel any pending searches
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                // Create a new search with delay (debounce)
                searchRunnable = () -> {
                    String query = s.toString().trim();
                    if (!query.isEmpty()) {
                        viewModel.searchBooks(query);
                        textResults.setText("Search Results");
                    } else if (viewModel.getCategories().getValue() != null && !viewModel.getCategories().getValue().isEmpty()) {
                        // If search is empty but categories are selected, search by categories
                        viewModel.searchBooks("");
                        textResults.setText("Filtered Results");
                    } else {
                        // No search and no categories, show popular books
                        viewModel.loadPopularBooks();
                        textResults.setText("Popular Books");
                    }
                };
                
                // Execute search after delay (500ms)
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });
        
        // Handle keyboard search button
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = editTextSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    viewModel.searchBooks(query);
                    textResults.setText("Search Results");
                }
                return true;
            }
            return false;
        });
        
        // All categories chip - simple direct click handling
        chipAll.setOnClickListener(v -> {
            Log.d(TAG, "All chip clicked");
            
            // Set state
            chipAll.setChecked(true);
            
            // Clear other chips
            for (Chip chip : categoryChips.values()) {
                chip.setChecked(false);
            }
            
            // Clear category filter in viewModel
            viewModel.clearCategorySelections();
            Log.d(TAG, "All chip selected, cleared category selections");
            
            // Trigger appropriate search
            String query = editTextSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                viewModel.searchBooks(query);
            } else {
                viewModel.loadPopularBooks();
                textResults.setText("Popular Books");
            }
        });
    }
    
    private void observeViewModel() {
        // Observe search results
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), books -> {
            if (books != null) {
                searchAdapter.setBooks(books);
                
                // Show/hide empty state
                emptyState.setVisibility(books.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerViewSearchResults.setVisibility(books.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
        
        // Observe categories to populate filter chips
        viewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                setupCategoryChips(categories);
            }
        });
        
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }
    
    private void setupCategoryChips(List<Category> categories) {
        // Clear existing dynamically added chips
        for (Chip chip : categoryChips.values()) {
            chipGroup.removeView(chip);
        }
        categoryChips.clear();
        
        // Skip if no categories
        if (categories.isEmpty()) {
            return;
        }
        
        Log.d(TAG, "Setting up " + categories.size() + " category chips");
        
        // Add each category as a chip
        for (Category category : categories) {
            Chip chip = createCategoryChip(category);
            chipGroup.addView(chip);
            categoryChips.put(category.getId(), chip);
        }
    }
    
    private Chip createCategoryChip(Category category) {
        // Create a new Chip with Filter style (explicitly use Filter style for proper selection behavior)
        Chip chip = new Chip(requireContext());
        chip.setCheckable(true);
        chip.setClickable(true);
        
        // Set the text and appearance
        chip.setText(category.getName());
        chip.setTextColor(Color.WHITE);
        
        // Set the chip background color
        chip.setChipBackgroundColorResource(R.color.primary_color);
        
        // Configure check icon
        chip.setCheckedIconVisible(true);
        
        // Set click listener - simple direct approach
        chip.setOnClickListener(v -> {
            // Toggle checked state and handle selection
            boolean isChecked = !chip.isChecked();
            chip.setChecked(isChecked);
            
            Log.d(TAG, "Category chip " + category.getName() + " clicked, setting checked: " + isChecked);
            
            if (isChecked) {
                // Uncheck "All" chip
                chipAll.setChecked(false);
                
                // Add this category to filter
                viewModel.toggleCategorySelection(category.getId());
                Log.d(TAG, "Added category to filter: " + category.getId());
            } else {
                // Check if all category chips are unchecked
                boolean anyChecked = false;
                for (Chip c : categoryChips.values()) {
                    if (c != chip && c.isChecked()) {
                        anyChecked = true;
                        break;
                    }
                }
                
                // If all category chips are unchecked, check the "All" chip
                if (!anyChecked) {
                    chipAll.setChecked(true);
                    viewModel.clearCategorySelections();
                    Log.d(TAG, "All chips unchecked, clearing selections");
                } else {
                    // Remove this category from filter
                    viewModel.toggleCategorySelection(category.getId());
                    Log.d(TAG, "Removed category from filter: " + category.getId());
                }
            }
        });
        
        return chip;
    }
    
    private void onBookClick(Book book) {
        Log.d(TAG, "Book clicked: " + book.getTitle());
        
        // Navigate to book detail using Navigation Component
        Bundle args = new Bundle();
        args.putString("book_id", book.getId());
        
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_navigation_book_search_to_bookDetailFragment, args);
    }
    
    private void onAddToCartClick(Book book) {
        Log.d(TAG, "Add to cart clicked: " + book.getTitle());
        
        if (book.getStock() <= 0) {
            Toast.makeText(getContext(), "Sorry, " + book.getTitle() + " is out of stock!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        boolean success = cartManager.addToCart(book, 1);
        if (success) {
            int cartCount = cartManager.getCartItemCount();
            Toast.makeText(getContext(), 
                book.getTitle() + " added to cart! (" + cartCount + " items)", 
                Toast.LENGTH_SHORT).show();
                
            // Refresh adapter to show updated cart status
            searchAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), 
                "Failed to add " + book.getTitle() + " to cart. Please try again.", 
                Toast.LENGTH_SHORT).show();
        }
    }
}