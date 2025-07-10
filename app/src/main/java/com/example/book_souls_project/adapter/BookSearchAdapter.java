package com.example.book_souls_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.book_souls_project.R;
import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.api.repository.CategoryRepository;
import com.example.book_souls_project.api.types.book.Book;
import com.example.book_souls_project.api.types.category.Category;
import com.example.book_souls_project.util.CartManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookSearchAdapter extends RecyclerView.Adapter<BookSearchAdapter.BookViewHolder> {

    private List<Book> books = new ArrayList<>();
    private OnBookClickListener onBookClickListener;
    private OnAddToCartClickListener onAddToCartClickListener;
    private CartManager cartManager;
    private CategoryRepository categoryRepository; // For fetching category names

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Book book);
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.onBookClickListener = listener;
    }

    public void setOnAddToCartClickListener(OnAddToCartClickListener listener) {
        this.onAddToCartClickListener = listener;
    }

    public void setCartManager(CartManager cartManager) {
        this.cartManager = cartManager;
    }
    
    public void setCategoryRepository(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public void setBooks(List<Book> books) {
        this.books = books != null ? books : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addBooks(List<Book> books) {
        if (books != null) {
            int startPosition = this.books.size();
            this.books.addAll(books);
            notifyItemRangeInserted(startPosition, books.size());
        }
    }

    public void clearBooks() {
        this.books.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_search, parent, false);
        return new BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = books.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageBookCover;
        private final TextView textBookTitle;
        private final TextView textBookAuthor;
        private final TextView textBookRating;
        private final TextView textRatingCount;
        private final TextView textBookGenre;
        private final TextView textBookPrice;
        private final TextView textBookDescription;
        private final ImageButton buttonWishlist;
        private final View buttonAddToCart;

        BookViewHolder(@NonNull View itemView) {
            super(itemView);
            imageBookCover = itemView.findViewById(R.id.imageBookCover);
            textBookTitle = itemView.findViewById(R.id.textBookTitle);
            textBookAuthor = itemView.findViewById(R.id.textBookAuthor);
            textBookRating = itemView.findViewById(R.id.textBookRating);
            textRatingCount = itemView.findViewById(R.id.textRatingCount);
            textBookGenre = itemView.findViewById(R.id.textBookGenre);
            textBookPrice = itemView.findViewById(R.id.textBookPrice);
            textBookDescription = itemView.findViewById(R.id.textBookDescription);
            buttonWishlist = itemView.findViewById(R.id.buttonWishlist);
            buttonAddToCart = itemView.findViewById(R.id.buttonAddToCart);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onBookClickListener != null) {
                    onBookClickListener.onBookClick(books.get(position));
                }
            });

            buttonAddToCart.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onAddToCartClickListener != null) {
                    onAddToCartClickListener.onAddToCartClick(books.get(position));
                }
            });
            
            // Wishlist feature is not implemented yet
            buttonWishlist.setVisibility(View.GONE);
        }

        void bind(Book book) {
            // Set book data to views
            textBookTitle.setText(book.getTitle());
            textBookAuthor.setText(book.getAuthor());
            
            // Format rating
            if (book.getRating() > 0) {
                textBookRating.setText(String.format(Locale.getDefault(), "%.1f", book.getRating()));
                textRatingCount.setText(String.format(Locale.getDefault(), "(%d)", book.getRatingCount()));
            } else {
                textBookRating.setText("N/A");
                textRatingCount.setText("(0)");
            }
            
            // Format price with VND currency
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedPrice = currencyFormat.format(book.getPrice());
            textBookPrice.setText(formattedPrice);
            
            // Set description
            textBookDescription.setText(book.getDescription());
            
            // Set genre/category with actual category name
            if (book.getCategoryIds() != null && !book.getCategoryIds().isEmpty() && categoryRepository != null) {
                String firstCategoryId = book.getCategoryIds().get(0);
                
                // Initially set to "Loading..." 
                textBookGenre.setText("Loading...");
                textBookGenre.setVisibility(View.VISIBLE);
                
                // Fetch the actual category name
                categoryRepository.getCategoryById(firstCategoryId, new CategoryRepository.CategoryDetailCallback() {
                    @Override
                    public void onCategoryLoaded(Category category) {
                        textBookGenre.setText(category.getName());
                    }

                    @Override
                    public void onError(String error) {
                        // Fallback to a generic label on error
                        textBookGenre.setText("Book Category");
                    }
                });
            } else {
                textBookGenre.setVisibility(View.GONE);
            }
            
            // Load book cover image
            if (book.getImage() != null && !book.getImage().isEmpty()) {
                Glide.with(imageBookCover.getContext())
                    .load(book.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.booksouls_logo_new)
                    .error(R.drawable.booksouls_logo_new)
                    .into(imageBookCover);
            } else {
                imageBookCover.setImageResource(R.drawable.booksouls_logo_new);
            }
            
            // Update add to cart button based on stock
            buttonAddToCart.setEnabled(book.getStock() > 0);
            buttonAddToCart.setAlpha(book.getStock() > 0 ? 1.0f : 0.5f);
        }
    }
}
