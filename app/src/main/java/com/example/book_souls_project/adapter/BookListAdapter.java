package com.example.book_souls_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_souls_project.R;
import com.example.book_souls_project.api.types.book.Book;
import com.example.book_souls_project.util.CartManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.BookViewHolder> {

    private List<Book> books = new ArrayList<>();
    private OnBookClickListener onBookClickListener;
    private OnAddToCartClickListener onAddToCartClickListener;
    private CartManager cartManager;

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
        books.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_list, parent, false);
        return new BookViewHolder(view);
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
        private ImageView imageBookCover;
        private TextView textBookTitle;
        private TextView textBookAuthor;
        private TextView textBookPrice;
        private TextView textRating, textRatingCount;
        private ImageButton buttonAddToCart;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            imageBookCover = itemView.findViewById(R.id.imageBookCover);
            textBookTitle = itemView.findViewById(R.id.textBookTitle);
            textBookAuthor = itemView.findViewById(R.id.textBookAuthor);
            textBookPrice = itemView.findViewById(R.id.textBookPrice);
            textRating = itemView.findViewById(R.id.textRating);
            textRatingCount = itemView.findViewById(R.id.textRatingCount);
            buttonAddToCart = itemView.findViewById(R.id.buttonAddToCart);

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (onBookClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onBookClickListener.onBookClick(books.get(position));
                    }
                }
            });

            if (buttonAddToCart != null) {
                buttonAddToCart.setOnClickListener(v -> {
                    if (onAddToCartClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onAddToCartClickListener.onAddToCartClick(books.get(position));
                        }
                    }
                });
            }
        }

        public void bind(Book book) {
            // Set book title
            textBookTitle.setText(book.getTitle() != null ? book.getTitle() : "Unknown Title");
            
            // Set book author
            textBookAuthor.setText(book.getAuthor() != null ? book.getAuthor() : "Unknown Author");

            // Set rating count
            textRatingCount.setText("(" + book.getRatingCount() + " reviews)");
            
            // Set book price
            if (book.getPrice() > 0) {
                textBookPrice.setText(String.format(Locale.getDefault(), "₫%,d", book.getPrice()));
            } else {
                textBookPrice.setText("Free");
            }
            
            // Set rating
            if (textRating != null) {
                if (book.getRating() > 0) {
                    textRating.setText(String.format(Locale.getDefault(), "%.1f", book.getRating()));
                } else {
                    textRating.setText("N/A");
                }
            }

            // Update add to cart button based on stock
            if (buttonAddToCart != null) {
                if (book.getStock() <= 0) {
                    buttonAddToCart.setEnabled(false);
                    buttonAddToCart.setAlpha(0.5f);
                } else {
                    buttonAddToCart.setEnabled(true);
                    buttonAddToCart.setAlpha(1.0f);
                }
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
        }
    }
}
