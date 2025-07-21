
package com.example.book_souls_project.fragment.book;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_souls_project.R;
import com.example.book_souls_project.adapter.BookFeaturedAdapter;
import com.example.book_souls_project.adapter.ReviewAdapter;
import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.api.repository.BookRepository;
import com.example.book_souls_project.api.repository.CategoryRepository;
import com.example.book_souls_project.api.repository.PublisherRepository;
import com.example.book_souls_project.api.repository.ReviewRepository;
import com.example.book_souls_project.api.types.book.Book;
import com.example.book_souls_project.api.types.category.Category;
import com.example.book_souls_project.api.types.publisher.Publisher;
import com.example.book_souls_project.api.types.review.Review;
import com.example.book_souls_project.api.types.review.ReviewListResponse;
import com.example.book_souls_project.api.types.review.ReviewCreateRequest;
import com.example.book_souls_project.api.types.review.ReviewCreateResponse;
import com.example.book_souls_project.util.CartManager;
import com.example.book_souls_project.util.TokenManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookDetailFragment extends Fragment {

    private static final String TAG = "BookDetailFragment";
    private static final String ARG_BOOK_ID = "book_id";

    // UI Components
    private ImageView imageBookCover;
    private TextView textBookTitle;
    private TextView textBookAuthor;
    private TextView textBookPrice;
    private TextView textBookRating;
    private TextView textRatingCount;
    private TextView textPublisher;
    private TextView textCategory;
    private TextView textReleaseYear;
    private TextView textStock;
    private TextView textIsbn;
    private TextView textSynopsis;
    private LinearLayout layoutIsbn;
    private MaterialButton buttonAddToCart;
    private MaterialButton buttonBuyNow;

//    private MaterialButton buttonWishlist;

    private RecyclerView recyclerViewRelatedBooks;
    
    // Review components
    private RatingBar ratingBarUserRating;
    private float selectedRating = 0; // Track the selected rating (0-5, supports 0.5 increments)
    private TextInputEditText editTextComment;
    private MaterialButton buttonSubmitReview;
    private RecyclerView recyclerViewReviews;
    private MaterialButton buttonLoadMoreReviews;
    private TextView textReviewsTitle;
    private TextView textNoReviews;

    // Data
    private String bookId;
    private Book currentBook;
    private BookRepository bookRepository;
    private CategoryRepository categoryRepository;
    private PublisherRepository publisherRepository;
    private ReviewRepository reviewRepository;
    private BookFeaturedAdapter relatedBooksAdapter;
    private ReviewAdapter reviewAdapter;
    private CartManager cartManager;
    private TokenManager tokenManager;
    
    // Review pagination
    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;
    private boolean isLoadingReviews = false;

    public static BookDetailFragment newInstance(String bookId) {
        BookDetailFragment fragment = new BookDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BOOK_ID, bookId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getString(ARG_BOOK_ID);
        }
        
        // Initialize repositories
        bookRepository = ApiRepository.getInstance(requireContext()).getBookRepository();
        categoryRepository = ApiRepository.getInstance(requireContext()).getCategoryRepository();
        publisherRepository = ApiRepository.getInstance(requireContext()).getPublisherRepository();
        reviewRepository = ApiRepository.getInstance(requireContext()).getReviewRepository();
        
        // Initialize cart manager
        cartManager = new CartManager(requireContext());
        
        // Initialize token manager
        tokenManager = new TokenManager(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_detail, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (bookId != null) {
            loadBookDetails();
            loadReviews(); // Load initial reviews
        } else {
            showError("Book ID is missing");
        }
        
        loadRelatedBooks();
    }

    private void initViews(View view) {
        imageBookCover = view.findViewById(R.id.imageBookCover);
        textBookTitle = view.findViewById(R.id.textBookTitle);
        textBookAuthor = view.findViewById(R.id.textBookAuthor);
        textBookPrice = view.findViewById(R.id.textBookPrice);
        textBookRating = view.findViewById(R.id.textBookRating);
        textRatingCount = view.findViewById(R.id.textRatingCount);
        textPublisher = view.findViewById(R.id.textPublisher);
        textCategory = view.findViewById(R.id.textCategory);
        textReleaseYear = view.findViewById(R.id.textReleaseYear);
        textStock = view.findViewById(R.id.textStock);
        textIsbn = view.findViewById(R.id.textIsbn);
        textSynopsis = view.findViewById(R.id.textSynopsis);
        layoutIsbn = view.findViewById(R.id.layoutIsbn);
        buttonAddToCart = view.findViewById(R.id.buttonAddToCart);
        buttonBuyNow = view.findViewById(R.id.buttonBuyNow);

//        buttonWishlist = view.findViewById(R.id.buttonWishlist);
        recyclerViewRelatedBooks = view.findViewById(R.id.recyclerViewRelatedBooks);

        // Initialize review components
        // Initialize star rating system
        ratingBarUserRating = view.findViewById(R.id.ratingBarUserRating);
        
        editTextComment = view.findViewById(R.id.editTextComment);
        buttonSubmitReview = view.findViewById(R.id.buttonSubmitReview);
        recyclerViewReviews = view.findViewById(R.id.recyclerViewReviews);
        buttonLoadMoreReviews = view.findViewById(R.id.buttonLoadMoreReviews);
        textReviewsTitle = view.findViewById(R.id.textTotalReviews);
        textNoReviews = view.findViewById(R.id.textNoReviews);

        // Setup star rating clicks
        setupStarRating();

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigateUp();
        });
    }

    private void setupRecyclerView() {
        relatedBooksAdapter = new BookFeaturedAdapter();
        recyclerViewRelatedBooks.setLayoutManager(
            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerViewRelatedBooks.setAdapter(relatedBooksAdapter);
        
        relatedBooksAdapter.setOnBookClickListener(book -> {
            // Navigate to another book detail using Navigation Component
            Bundle args = new Bundle();
            args.putString("book_id", book.getId());
            
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_BookDetail_to_BookDetail, args);
        });
        
        // Setup reviews RecyclerView
        reviewAdapter = new ReviewAdapter();
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewReviews.setAdapter(reviewAdapter);
    }

    private void setupClickListeners() {
        buttonAddToCart.setOnClickListener(v -> {
            if (currentBook != null) {
                addToCart(currentBook);
            }
        });

        buttonBuyNow.setOnClickListener(v -> {
            if (currentBook != null) {
                buyNow(currentBook);
            }
        });

//        buttonWishlist.setOnClickListener(v -> {
//            if (currentBook != null) {
//                addToWishlist(currentBook);
//            }
//        });

        // Review submission
        buttonSubmitReview.setOnClickListener(v -> {
            if (currentBook != null) {
                submitReview();
            }
        });
    }

    private void setupStarRating() {
        ratingBarUserRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    selectedRating = rating;
                }
            }
        });
    }
    
    private void updateStarDisplay() {
        ratingBarUserRating.setRating(selectedRating);
    }

    private void loadBookDetails() {
        Log.d(TAG, "Loading book details for ID: " + bookId);
        
        bookRepository.getBookById(bookId, new BookRepository.BookDetailCallback() {
            @Override
            public void onBookLoaded(Book book) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentBook = book;
                        displayBookDetails(book);
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading book details: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showError("Failed to load book details: " + error);
                    });
                }
            }

            @Override
            public void onLoading() {
                Log.d(TAG, "Loading book details...");
            }
        });
    }

    private void displayBookDetails(Book book) {
        // Set book information
        textBookTitle.setText(book.getTitle());
        textBookAuthor.setText(book.getAuthor());
        
        // Format price
        if (book.getPrice() > 0) {
            textBookPrice.setText(String.format(Locale.getDefault(), "₫%,d", book.getPrice()));
        } else {
            textBookPrice.setText("Free");
        }
        
        // Set rating
        if (book.getRating() > 0) {
            textBookRating.setText(String.format(Locale.getDefault(), "%.1f", book.getRating()));
            textRatingCount.setText(String.format(Locale.getDefault(), "(%d)", book.getRatingCount()));
        } else {
            textBookRating.setText("N/A");
            textRatingCount.setText("(0)");
        }
        
        // Set release year
        textReleaseYear.setText(String.valueOf(book.getReleaseYear()));
        
        // Set stock
        if (book.getStock() > 0) {
            textStock.setText(String.format(Locale.getDefault(), "%d copies available", book.getStock()));
            textStock.setTextColor(getResources().getColor(R.color.success_color, null));
        } else {
            textStock.setText("Out of stock");
            textStock.setTextColor(getResources().getColor(R.color.error_color, null));
            buttonAddToCart.setEnabled(false);
            buttonBuyNow.setEnabled(false);
        }
        
        // Set ISBN if available
        if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
            textIsbn.setText(book.getIsbn());
            layoutIsbn.setVisibility(View.VISIBLE);
        } else {
            layoutIsbn.setVisibility(View.GONE);
        }
        
        // Set synopsis using book description or fallback text
        if (book.getDescription() != null && !book.getDescription().isEmpty()) {
            textSynopsis.setText(book.getDescription());
        } else {
            textSynopsis.setText("Discover this amazing book by " + book.getAuthor() + 
                ". Published in " + book.getReleaseYear() + 
                ", this book offers valuable insights and knowledge in its field. " +
                "Experience quality content that has been carefully curated for readers.");
        }
        
        // Load book cover image
        if (book.getImage() != null && !book.getImage().isEmpty()) {
            Glide.with(this)
                    .load(book.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.booksouls_logo_new)
                    .error(R.drawable.booksouls_logo_new)
                    .into(imageBookCover);
        } else {
            imageBookCover.setImageResource(R.drawable.booksouls_logo_new);
        }
        
        // Load additional details (category and publisher)
        loadCategoryDetails(book);
        
        // Update cart button text
        updateCartButtonText();
    }
    
    private void updateCartButtonText() {
        if (currentBook != null) {
            boolean isInCart = cartManager.isInCart(currentBook.getId());
            int quantity = cartManager.getBookQuantityInCart(currentBook.getId());
            
            if (isInCart) {
                buttonAddToCart.setText("In Cart (" + quantity + ")");
                buttonAddToCart.setEnabled(true);
            } else {
                buttonAddToCart.setText("Add to Cart");
                buttonAddToCart.setEnabled(currentBook.getStock() > 0);
            }
        }
    }

    private void loadCategoryDetails(Book book) {
        // Set placeholder first
        textPublisher.setText("Loading...");
        textCategory.setText("Loading...");
        
        // Load category details if categoryIds exist
        if (book.getCategoryIds() != null && !book.getCategoryIds().isEmpty()) {
            String firstCategoryId = book.getCategoryIds().get(0); // Use first category
            
            categoryRepository.getCategoryById(firstCategoryId, new CategoryRepository.CategoryDetailCallback() {
                @Override
                public void onCategoryLoaded(Category category) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            textCategory.setText(category.getName());
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error loading category details: " + error);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            textCategory.setText("Unknown Category");
                        });
                    }
                }
            });
        } else {
            textCategory.setText("No Category");
        }
        
        // Load publisher details if publisherId exists
        if (book.getPublisherId() != null && !book.getPublisherId().isEmpty()) {
            publisherRepository.getPublisherById(book.getPublisherId()).observe(this, publisher -> {
                if (publisher != null) {
                    textPublisher.setText(publisher.getName());
                } else {
                    textPublisher.setText("Unknown Publisher");
                }
            });
        } else {
            textPublisher.setText("No Publisher");
        }
    }

    private void loadRelatedBooks() {
        // Load all books and show as related (excluding current book)
        bookRepository.getBooks(new BookRepository.BookCallback() {
            @Override
            public void onBooksLoaded(com.example.book_souls_project.api.types.book.BookListResponse response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        List<Book> allBooks = response.getResult().getItems();
                        List<Book> relatedBooks = new ArrayList<>();
                        
                        // Filter out current book and limit to 10 books
                        for (Book book : allBooks) {
                            if (!book.getId().equals(bookId) && relatedBooks.size() < 10) {
                                relatedBooks.add(book);
                            }
                        }
                        
                        relatedBooksAdapter.setBooks(relatedBooks);
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading related books: " + error);
            }
        });
    }

    private void addToCart(Book book) {
        Log.d(TAG, "Add to cart: " + book.getTitle());
        
        // Check if book is available
        if (book.getStock() <= 0) {
            Toast.makeText(getContext(), "This book is out of stock!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Add book to cart with quantity 1
        boolean success = cartManager.addToCart(book, 1);
        
        if (success) {
            Toast.makeText(getContext(), book.getTitle() + " added to cart!", Toast.LENGTH_SHORT).show();
            updateCartButtonText();
        } else {
            Toast.makeText(getContext(), "Failed to add book to cart!", Toast.LENGTH_SHORT).show();
        }
    }

    private void buyNow(Book book) {
        Log.d(TAG, "Buy now: " + book.getTitle());
        
        // Check if book is available
        if (book.getStock() <= 0) {
            Toast.makeText(getContext(), "This book is out of stock!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Add to cart first if not already in cart
        if (!cartManager.isInCart(book.getId())) {
            cartManager.addToCart(book, 1);
        }
        
        // Show buy now message
        Toast.makeText(getContext(), "Redirecting to checkout for " + book.getTitle(), Toast.LENGTH_SHORT).show();
        
        // TODO: Navigate to checkout/payment screen
        }

    private void loadReviews() {
        if (isLoadingReviews) return;
        
        isLoadingReviews = true;
        
        reviewRepository.getBookReviews(bookId, new ReviewRepository.ReviewListCallback() {
            @Override
            public void onSuccess(ReviewListResponse response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        isLoadingReviews = false;
                        
                        // Use the convenience method getData() which internally calls result.getItems()
                        List<Review> reviews = response.getData();
                        int totalCount = response.getTotalCount();
                        
                        if (reviews != null && !reviews.isEmpty()) {
                            reviewAdapter.setReviews(reviews);
                            
                            // Update reviews title with count
                            textReviewsTitle.setText("Reviews (" + totalCount + ")");
                            
                            // Show reviews RecyclerView, hide no reviews message
                            recyclerViewReviews.setVisibility(View.VISIBLE);
                            textNoReviews.setVisibility(View.GONE);
                            
                            // Hide load more button since we're loading all reviews at once
                            buttonLoadMoreReviews.setVisibility(View.GONE);
                        } else {
                            // No reviews found
                            reviewAdapter.setReviews(new ArrayList<>());
                            textReviewsTitle.setText("0 reviews");
                            
                            // Hide reviews RecyclerView, show no reviews message
                            recyclerViewReviews.setVisibility(View.GONE);
                            textNoReviews.setVisibility(View.VISIBLE);
                            
                            buttonLoadMoreReviews.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        isLoadingReviews = false;
                        Log.e(TAG, "Error loading reviews: " + error);
                        
                        // Set empty state for reviews
                        reviewAdapter.setReviews(new ArrayList<>());
                        textReviewsTitle.setText("0 reviews");
                        
                        // Hide reviews RecyclerView, show no reviews message
                        recyclerViewReviews.setVisibility(View.GONE);
                        textNoReviews.setVisibility(View.VISIBLE);
                        
                        buttonLoadMoreReviews.setVisibility(View.GONE);
                        
                        Toast.makeText(getContext(), "Failed to load reviews: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void submitReview() {
        float rating = selectedRating;
        String comment = editTextComment.getText() != null ? editTextComment.getText().toString().trim() : "";
        
        // Validate input
        if (rating == 0) {
            Toast.makeText(getContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (comment.isEmpty()) {
            Toast.makeText(getContext(), "Please write a comment", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Get current logged-in user ID from TokenManager
        String userId = tokenManager.getUserId();
        
        // Check if user is logged in
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getContext(), "Please log in to submit a review", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Disable submit button to prevent double submission
        buttonSubmitReview.setEnabled(false);
        buttonSubmitReview.setText("Submitting...");
        
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setBookId(bookId);
        request.setUserId(userId);
        request.setRating((double) rating); // Convert float to double
        request.setComment(comment);
        
        reviewRepository.createReview(request, new ReviewRepository.ReviewCreateCallback() {
            @Override
            public void onSuccess(ReviewCreateResponse response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        buttonSubmitReview.setEnabled(true);
                        buttonSubmitReview.setText("Submit Review");
                        
                        // Clear form
                        selectedRating = 0;
                        updateStarDisplay();
                        editTextComment.setText("");
                        
                        // Show success toast
                        Toast.makeText(getContext(), "Comment created successfully!", Toast.LENGTH_SHORT).show();
                        
                        // Reload reviews to show the new one
                        loadReviews();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        buttonSubmitReview.setEnabled(true);
                        buttonSubmitReview.setText("Submit Review");
                        
                        Log.e(TAG, "Error submitting review: " + error);
                        // Show error toast
                        Toast.makeText(getContext(), "Failed to create comment: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigateUp();
    }
}
