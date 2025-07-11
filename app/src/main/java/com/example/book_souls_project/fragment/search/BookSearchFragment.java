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

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
public class BookSearchFragment extends Fragment {

    
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
    private ProgressBar loadingMoreProgressBar;
    private androidx.core.widget.NestedScrollView nestedScrollView;
    private boolean isLoadingMore = false;
    private int limit = 10;
    
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

    
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(BookSearchViewModel.class);
        
        // Initialize CartManager
        cartManager = CartManager.getInstance(requireContext());
        
        // Get limit from ViewModel
        limit = viewModel.getLimit();
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Setup scroll listener
        setupScrollListener();
        
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
        loadingMoreProgressBar = view.findViewById(R.id.loadingMoreProgressBar);
        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        
        // Configure the "All" chip to ensure check icon is visible
        chipAll.setCheckable(true);
        chipAll.setClickable(true);
        chipAll.setChecked(true); // Make sure it's checked by default
        chipAll.setCheckedIconVisible(true);
        chipAll.setCheckedIcon(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_check));
        chipAll.setCheckedIconTint(ColorStateList.valueOf(Color.WHITE));
        

        
        // Initially set results text
        textResults.setText("Popular Books");
    }
    
    private void setupRecyclerView() {
        searchAdapter = new BookSearchAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        layoutManager.setAutoMeasureEnabled(true);
        recyclerViewSearchResults.setLayoutManager(layoutManager);
        recyclerViewSearchResults.setAdapter(searchAdapter);
        recyclerViewSearchResults.setNestedScrollingEnabled(false); // Allow parent NestedScrollView to handle scrolling
        
        // Set cart manager for adapter
        searchAdapter.setCartManager(cartManager);
        
        // Set category repository for fetching category names
        CategoryRepository categoryRepository = ApiRepository.getInstance(requireContext()).getCategoryRepository();
        searchAdapter.setCategoryRepository(categoryRepository);
        
        // Set click listeners for book items
        searchAdapter.setOnBookClickListener(this::onBookClick);
        searchAdapter.setOnAddToCartClickListener(this::onAddToCartClick);
    }
    
    private void setupScrollListener() {
        // For RecyclerView scroll events
        recyclerViewSearchResults.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                checkIfShouldLoadMore();
            }
        });
        
        // For NestedScrollView scroll events
        nestedScrollView.setOnScrollChangeListener(new androidx.core.widget.NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(androidx.core.widget.NestedScrollView v, int scrollX, int scrollY, 
                                      int oldScrollX, int oldScrollY) {
                // Check if we've scrolled to the bottom
                if (!isLoadingMore && scrollY > oldScrollY) {
                    // Calculate if we're near the bottom
                    int childHeight = nestedScrollView.getChildAt(0).getHeight();
                    int scrollViewHeight = nestedScrollView.getHeight();
                    int scrollThreshold = childHeight - scrollViewHeight - 200; // 200px threshold
                    
                    if (scrollY >= scrollThreshold && viewModel.hasMoreData()) {
                        loadMoreBooks();
                    }
                }
            }
        });
    }
    
    private void checkIfShouldLoadMore() {
        if (isLoadingMore || !viewModel.hasMoreData()) {
            return;
        }
        
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewSearchResults.getLayoutManager();
        if (layoutManager != null) {
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
            
            // Check if we're near the bottom of the list
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3 &&
                    firstVisibleItemPosition >= 0 &&
                    totalItemCount >= limit) {
                loadMoreBooks();
            }
        }
    }
    
    private void loadMoreBooks() {
        // Prevent multiple simultaneous load more requests
        if (isLoadingMore) {
            return;
        }
        
        isLoadingMore = true;
        loadingMoreProgressBar.setVisibility(View.VISIBLE);
        searchAdapter.setLoading(true);
        
        // Add a slight delay for better UX and to avoid rapid sequential calls
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            viewModel.loadMoreBooks();
        }, 300);
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
        
        // All categories chip - Use OnCheckedChangeListener instead of OnClickListener
        CompoundButton.OnCheckedChangeListener allChipListener = (buttonView, isChecked) -> {

            
            if (isChecked) {

                // Uncheck all category chips without triggering their listeners
                for (Chip chip : categoryChips.values()) {
                    // Remove listener temporarily
                    CompoundButton.OnCheckedChangeListener oldListener = (CompoundButton.OnCheckedChangeListener) chip.getTag();
                    if (oldListener != null) {
                        chip.setOnCheckedChangeListener(null);
                    }
                    
                    // Uncheck the chip
                    chip.setChecked(false);

                    
                    // Restore the listener
                    if (oldListener != null) {
                        chip.setOnCheckedChangeListener(oldListener);
                    }
                }
                
                // Clear category selections and trigger search
                viewModel.clearCategorySelections();

            } else if (areAllCategoryChipsUnchecked()) {
                // If no other chips are checked, we should keep "All" checked

                chipAll.setChecked(true);
            }
        };
        
        // Store the listener as a tag for later reference
        chipAll.setTag(allChipListener);
        chipAll.setOnCheckedChangeListener(allChipListener);
        

    }
    
    private void observeViewModel() {
        // Observe search results
        viewModel.getSearchResults().observe(getViewLifecycleOwner(), books -> {
            if (books != null) {
                searchAdapter.setBooks(books);
                
                // Show/hide empty state based on search results
                boolean isEmpty = books.isEmpty();
                emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                recyclerViewSearchResults.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                
                // Update header text based on search results
                if (isEmpty && !viewModel.getCurrentSearchQuery().isEmpty()) {
                    textResults.setText("No results found");
                } else if (!isEmpty && !viewModel.getCurrentSearchQuery().isEmpty()) {
                    textResults.setText("Search results");
                } else {
                    textResults.setText("Popular Books");
                }
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
        
        // Observe loading more state
        viewModel.getIsLoadingMore().observe(getViewLifecycleOwner(), isLoadingMore -> {
            this.isLoadingMore = isLoadingMore;
            loadingMoreProgressBar.setVisibility(isLoadingMore ? View.VISIBLE : View.GONE);
            
            // Update adapter loading state
            searchAdapter.setLoading(isLoadingMore);
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
        
        // Add each category as a chip
        for (Category category : categories) {
            Chip chip = createCategoryChip(category);
            chipGroup.addView(chip);
            categoryChips.put(category.getId(), chip);
        }
    }
    
    private Chip createCategoryChip(Category category) {

        
        // Create chip with the custom style we defined
        Chip chip = new Chip(requireContext(), null, R.style.CategoryChipStyle);
        chip.setText(category.getName());
        chip.setCheckable(true);
        
        // Ensure chip is clickable and focused properly
        chip.setClickable(true);
        chip.setFocusable(true);
        chip.setChipIconVisible(false);
        chip.setCheckedIconVisible(true);
        chip.setCheckedIcon(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_check));
        
        // DEBUGGING: Check if the drawable is actually loaded
        if (chip.getCheckedIcon() == null) {

        } else {

        }
        
        // Set chip background color state list for the checked/unchecked states
        int[][] states = new int[][] {
            new int[] { android.R.attr.state_checked },  // checked state
            new int[] { -android.R.attr.state_checked }  // unchecked state
        };
        
        int[] colors = new int[] {
            getResources().getColor(R.color.primary_color_dark, null),  // darker color when checked
            getResources().getColor(R.color.primary_color, null)       // normal color when unchecked
        };
        
        ColorStateList colorStateList = new ColorStateList(states, colors);
        chip.setChipBackgroundColor(colorStateList);
        
        // Set text color to white for better visibility
        chip.setTextColor(ColorStateList.valueOf(Color.WHITE));
        
        // No close icon
        chip.setCloseIconVisible(false);
        
        // Create and store the listener
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {

            
            if (isChecked) {
                // Temporarily remove All chip listener to avoid callbacks
                CompoundButton.OnCheckedChangeListener allChipListener = 
                    (CompoundButton.OnCheckedChangeListener) chipAll.getTag();
                chipAll.setOnCheckedChangeListener(null);
                
                // Uncheck "All" chip when any other chip is checked
                chipAll.setChecked(false);

                
                // Restore All chip listener
                if (allChipListener != null) {
                    chipAll.setOnCheckedChangeListener(allChipListener);
                }
                
                // Add this category to filter
                viewModel.toggleCategorySelection(category.getId());

            } else {
                // Check if all category chips are unchecked
                boolean anyChecked = false;
                for (Chip c : categoryChips.values()) {
                    if (c.isChecked()) {
                        anyChecked = true;

                        break;
                    }
                }
                
                // If all category chips are unchecked, check the "All" chip
                if (!anyChecked) {
                    // Temporarily remove All chip listener to avoid callbacks
                    CompoundButton.OnCheckedChangeListener allChipListener = 
                        (CompoundButton.OnCheckedChangeListener) chipAll.getTag();
                    chipAll.setOnCheckedChangeListener(null);
                    
                    chipAll.setChecked(true);
                    
                    // Restore All chip listener
                    if (allChipListener != null) {
                        chipAll.setOnCheckedChangeListener(allChipListener);
                    }
                    
                    viewModel.clearCategorySelections();

                } else {
                    // Remove this category from filter
                    viewModel.toggleCategorySelection(category.getId());

                }
            }
        };
        
        // Store the listener as a tag for later reference
        chip.setTag(listener);
        
        // Set the click listener
        chip.setOnCheckedChangeListener(listener);
        
        // Final debug check to make sure chip is properly configured

        
        return chip;
    }
    
    private void onBookClick(Book book) {

        
        // Navigate to book detail using Navigation Component
        Bundle args = new Bundle();
        args.putString("book_id", book.getId());
        
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_navigation_book_search_to_bookDetailFragment, args);
    }
    
    private void onAddToCartClick(Book book) {

        
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
    
    private boolean areAllCategoryChipsUnchecked() {
        for (Chip chip : categoryChips.values()) {
            if (chip.isChecked()) {
                return false;
            }
        }
        return true;
    }
}
