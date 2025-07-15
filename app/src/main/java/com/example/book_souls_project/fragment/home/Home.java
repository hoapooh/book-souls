package com.example.book_souls_project.fragment.home;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.widget.NestedScrollView;

import com.example.book_souls_project.R;
import com.example.book_souls_project.adapter.BookFeaturedAdapter;
import com.example.book_souls_project.adapter.BookListAdapter;
import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.api.repository.BookRepository;
import com.example.book_souls_project.api.types.book.Book;
import com.example.book_souls_project.api.types.book.BookListResponse;
import com.example.book_souls_project.util.CartManager;

import java.util.List;

public class Home extends Fragment {

    private static final String TAG = "HomeFragment";
    
    private HomeViewModel mViewModel;
    private Button buttonTestLogin;
    
    // RecyclerViews
    private RecyclerView recyclerViewFeatured;
    private RecyclerView recyclerViewRecent;
    private RecyclerView recyclerViewCategories;
    
    // UI Components for loading and empty states
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private NestedScrollView contentScrollView;
    
    // UI Components for loading more indicator
    private ProgressBar loadingMoreProgressBar;
    private boolean isLoadingMore = false;
    
    // Pagination
    private int limit = 10;
    
    // Adapters
    private BookFeaturedAdapter featuredAdapter;
    private BookListAdapter recentAdapter;
    private com.example.book_souls_project.adapter.CategoryAdapter categoryAdapter;
    
    // Repository
    private BookRepository bookRepository;
    
    // Cart Manager
    private CartManager cartManager;

    public static Home newInstance() {
        return new Home();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize ViewModel first
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        // Initialize CartManager
        cartManager = new CartManager(requireContext());
        
        // Initialize repository
        bookRepository = ApiRepository.getInstance(requireContext()).getBookRepository();
        
        initViews(view);
        setupRecyclerViews();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get limit value from ViewModel
        limit = mViewModel.getLimit();
        
        setupClickListeners();
        setupScrollListener();
        observeViewModel();
        
        // Load books and categories using ViewModel
        mViewModel.loadBooks();
        mViewModel.loadCategories();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // Check if we need to refresh the data
        if (featuredAdapter != null && featuredAdapter.getItemCount() == 0) {
            // Reload data if the adapter is empty
            mViewModel.loadBooks();
            mViewModel.loadCategories();
        }
    }
    
    private void observeViewModel() {
        // Observe featured books
        mViewModel.getFeaturedBooks().observe(getViewLifecycleOwner(), books -> {
            if (books != null) {
                featuredAdapter.setBooks(books);
                
                // Show/hide empty state for featured books
                updateEmptyState(books);
            }
        });
        
        // Observe recent books
        mViewModel.getRecentBooks().observe(getViewLifecycleOwner(), books -> {
            if (books != null) {
                recentAdapter.setBooks(books);
            }
        });
        
        // Observe categories
        mViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoryAdapter.setCategories(categories);
            }
        });
        
        // Observe loading state
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Show/hide loading spinner
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            
            // Hide content when loading
            if (isLoading) {
                contentScrollView.setVisibility(View.GONE);
                emptyState.setVisibility(View.GONE);
            } else {
                // Content visibility will be managed in the books observer
                List<Book> featuredBooks = mViewModel.getFeaturedBooks().getValue();
                updateEmptyState(featuredBooks);
            }
        });
        
        // Observe loading more state
        mViewModel.getIsLoadingMore().observe(getViewLifecycleOwner(), isLoadingMore -> {
            this.isLoadingMore = isLoadingMore;
            loadingMoreProgressBar.setVisibility(isLoadingMore ? View.VISIBLE : View.GONE);
            
            // Update featured adapter loading state
            featuredAdapter.setLoading(isLoadingMore);
        });
        
        // Observe error messages
        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                mViewModel.clearError();
            }
        });
    }
    
    /**
     * Update UI based on book list status
     */
    private void updateEmptyState(List<Book> books) {
        if (books == null || books.isEmpty()) {
            // No books to display
            emptyState.setVisibility(View.VISIBLE);
            contentScrollView.setVisibility(View.GONE);
        } else {
            // Books available
            emptyState.setVisibility(View.GONE);
            contentScrollView.setVisibility(View.VISIBLE);
        }
    }
    
    private void initViews(View view) {
        buttonTestLogin = view.findViewById(R.id.buttonTestLogin);
        recyclerViewFeatured = view.findViewById(R.id.recyclerViewFeatured);
        recyclerViewRecent = view.findViewById(R.id.recyclerViewRecent);
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        
        // Initialize new UI components
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
        contentScrollView = view.findViewById(R.id.contentScrollView);
        loadingMoreProgressBar = view.findViewById(R.id.loadingMoreProgressBar);
    }
    
    private void setupRecyclerViews() {
        // Setup featured books RecyclerView (horizontal)
        featuredAdapter = new BookFeaturedAdapter();
        LinearLayoutManager featuredLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewFeatured.setLayoutManager(featuredLayoutManager);
        recyclerViewFeatured.setAdapter(featuredAdapter);
        
        // Add scroll listener to the featured books RecyclerView for horizontal pagination
        recyclerViewFeatured.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                // Check if scrolled to the end horizontally
                if (!isLoadingMore && dx > 0) { // Scrolling right and not already loading
                    int visibleItemCount = featuredLayoutManager.getChildCount();
                    int totalItemCount = featuredLayoutManager.getItemCount();
                    int firstVisibleItemPosition = featuredLayoutManager.findFirstVisibleItemPosition();
                    int lastVisibleItemPosition = featuredLayoutManager.findLastVisibleItemPosition();
                    
                    // If last item is visible and we have at least one full page of items
                    if (lastVisibleItemPosition >= totalItemCount - 3 && totalItemCount >= limit - 1) {
                        // Load more books if there are more
                        if (mViewModel.hasMoreData()) {
                            loadMoreBooks();
                        }
                    }
                }
            }
        });
        
        // Setup categories RecyclerView (horizontal)
        categoryAdapter = new com.example.book_souls_project.adapter.CategoryAdapter();
        LinearLayoutManager categoriesLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewCategories.setLayoutManager(categoriesLayoutManager);
        recyclerViewCategories.setAdapter(categoryAdapter);
        
        // Set click listener for categories
        categoryAdapter.setOnCategoryClickListener(this::onCategoryClick);
        
        // Setup recent books RecyclerView (vertical)
        recentAdapter = new BookListAdapter();
        recyclerViewRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecent.setAdapter(recentAdapter);
        
        // Set cart manager for both adapters
        featuredAdapter.setCartManager(cartManager);
        recentAdapter.setCartManager(cartManager);
        
        // Set click listeners for book items
        featuredAdapter.setOnBookClickListener(this::onBookClick);
        featuredAdapter.setOnAddToCartClickListener(this::onAddToCartClick);
        
        recentAdapter.setOnBookClickListener(this::onBookClick);
        recentAdapter.setOnAddToCartClickListener(this::onAddToCartClick);
    }
    
    private void setupClickListeners() {
        // Handle test button click to go back to login
        buttonTestLogin.setOnClickListener(v -> {
            NavHostFragment.findNavController(Home.this).navigate(R.id.action_Home_to_LoginFragment);
        });
    }
    
    private void setupScrollListener() {
        // Monitor scroll to load more when reaching bottom
        contentScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) 
            (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                // User is at the bottom of the scroll view
                if (mViewModel.hasMoreData() && !isLoadingMore) {
                    loadMoreBooks();
                }
            }
        });
    }
    
    private void loadMoreBooks() {
        isLoadingMore = true;
        loadingMoreProgressBar.setVisibility(View.VISIBLE);
        mViewModel.loadMoreBooks();
    }
    
    private void onBookClick(Book book) {
        // Navigate to book detail using Navigation Component
        Bundle args = new Bundle();
        args.putString("book_id", book.getId());
        
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_Home_to_BookDetail, args);
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
            
            // Refresh adapters to show updated cart status
            featuredAdapter.notifyDataSetChanged();
            recentAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), 
                "Failed to add " + book.getTitle() + " to cart. Please try again.", 
                Toast.LENGTH_SHORT).show();
        }
    }
    
    private void onCategoryClick(com.example.book_souls_project.api.types.category.Category category) {
        // Navigate to search with the selected category
        Bundle args = new Bundle();
        args.putString("category_id", category.getId());
        
        NavController navController = NavHostFragment.findNavController(this);
        
        // Navigate to the search fragment
        
        navController.navigate(R.id.navigation_book_search, args,
            new androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.navigation_home, false)
                .build());
        
        
        // Get the activity and find the bottom navigation view
        if (getActivity() != null) {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.navigation_book_search);
            }
        }
        
    }
}