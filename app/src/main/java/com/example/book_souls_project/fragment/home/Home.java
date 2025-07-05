package com.example.book_souls_project.fragment.home;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.book_souls_project.R;
import com.example.book_souls_project.adapter.BookFeaturedAdapter;
import com.example.book_souls_project.adapter.BookListAdapter;
import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.api.repository.BookRepository;
import com.example.book_souls_project.api.types.book.Book;
import com.example.book_souls_project.api.types.book.BookListResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class Home extends Fragment {

    private static final String TAG = "HomeFragment";
    
    private HomeViewModel mViewModel;
    private Button buttonTestLogin;
    private FloatingActionButton fabSearch;
    
    // RecyclerViews
    private RecyclerView recyclerViewFeatured;
    private RecyclerView recyclerViewRecent;
    private RecyclerView recyclerViewCategories;
    
    // Adapters
    private BookFeaturedAdapter featuredAdapter;
    private BookListAdapter recentAdapter;
    
    // Repository
    private BookRepository bookRepository;

    public static Home newInstance() {
        return new Home();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        // Initialize ViewModel first
        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        // Initialize repository
        bookRepository = ApiRepository.getInstance(requireContext()).getBookRepository();
        
        initViews(view);
        setupRecyclerViews();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupClickListeners();
        observeViewModel();
        
        // Load books using ViewModel
        mViewModel.loadBooks();
    }
    
    private void observeViewModel() {
        // Observe featured books
        mViewModel.getFeaturedBooks().observe(getViewLifecycleOwner(), books -> {
            if (books != null) {
                Log.d(TAG, "Featured books updated: " + books.size() + " books");
                featuredAdapter.setBooks(books);
            }
        });
        
        // Observe recent books
        mViewModel.getRecentBooks().observe(getViewLifecycleOwner(), books -> {
            if (books != null) {
                Log.d(TAG, "Recent books updated: " + books.size() + " books");
                recentAdapter.setBooks(books);
            }
        });
        
        // Observe loading state
        mViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // TODO: Show/hide loading spinner
            Log.d(TAG, "Loading state: " + isLoading);
        });
        
        // Observe error messages
        mViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.e(TAG, "Error: " + error);
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                mViewModel.clearError();
            }
        });
    }
    
    private void initViews(View view) {
        buttonTestLogin = view.findViewById(R.id.buttonTestLogin);
        fabSearch = view.findViewById(R.id.fabSearch);
        recyclerViewFeatured = view.findViewById(R.id.recyclerViewFeatured);
        recyclerViewRecent = view.findViewById(R.id.recyclerViewRecent);
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
    }
    
    private void setupRecyclerViews() {
        // Setup featured books RecyclerView (horizontal)
        featuredAdapter = new BookFeaturedAdapter();
        recyclerViewFeatured.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerViewFeatured.setAdapter(featuredAdapter);
        
        // Setup recent books RecyclerView (vertical)
        recentAdapter = new BookListAdapter();
        recyclerViewRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecent.setAdapter(recentAdapter);
        
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
        
        // Handle search FAB click
        fabSearch.setOnClickListener(v -> {
            // TODO: Navigate to search fragment
            Toast.makeText(getContext(), "Search feature coming soon!", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void onBookClick(Book book) {
        Log.d(TAG, "Book clicked: " + book.getTitle());
        Toast.makeText(getContext(), "Opening: " + book.getTitle(), Toast.LENGTH_SHORT).show();
        // TODO: Navigate to book details fragment
    }
    
    private void onAddToCartClick(Book book) {
        Log.d(TAG, "Add to cart clicked: " + book.getTitle());
        Toast.makeText(getContext(), book.getTitle() + " added to cart!", Toast.LENGTH_SHORT).show();
        // TODO: Implement add to cart functionality
    }
}