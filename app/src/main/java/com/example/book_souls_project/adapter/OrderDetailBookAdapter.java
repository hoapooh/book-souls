package com.example.book_souls_project.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.book_souls_project.R;
import com.example.book_souls_project.api.service.BookService;
import com.example.book_souls_project.api.types.book.BookDetailResponse;
import com.example.book_souls_project.api.types.order.OrderBookDetail;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailBookAdapter extends RecyclerView.Adapter<OrderDetailBookAdapter.ViewHolder> {
    private static final String TAG = "OrderDetailBookAdapter";
    
    private Context context;
    private List<OrderBookDetail> orderBooks = new ArrayList<>();
    private BookService bookService;
    
    public OrderDetailBookAdapter(Context context, BookService bookService) {
        this.context = context;
        this.bookService = bookService;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail_book, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderBookDetail orderBook = orderBooks.get(position);
        
        // Set basic book info from order
        holder.textBookTitle.setText(orderBook.getBookTitle());
        holder.textQuantity.setText("Qty: " + orderBook.getQuantity());
        
        // Format price
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedPrice = "₫" + formatter.format(orderBook.getBookPrice());
        holder.textBookPrice.setText(formattedPrice);
        
        // Calculate total price for this book
        double totalBookPrice = orderBook.getBookPrice() * orderBook.getQuantity();
        String formattedTotalPrice = "₫" + formatter.format(totalBookPrice);
        holder.textTotalBookPrice.setText(formattedTotalPrice);
        
        // Load detailed book info for image and additional details
        loadBookDetails(orderBook.getBookId(), holder);
    }
    
    private void loadBookDetails(String bookId, ViewHolder holder) {
        bookService.getBookById(bookId).enqueue(new Callback<BookDetailResponse>() {
            @Override
            public void onResponse(Call<BookDetailResponse> call, Response<BookDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var bookDetail = response.body().getResult();
                    
                    // Load book image
                    if (bookDetail.getImage() != null && !bookDetail.getImage().isEmpty()) {
                        Glide.with(context)
                            .load(bookDetail.getImage())
                            .placeholder(R.drawable.ic_book_placeholder)
                            .error(R.drawable.ic_book_placeholder)
                            .into(holder.imageBookCover);
                    }
                    
                    // Set additional book info
                    holder.textBookAuthor.setText("by " + bookDetail.getAuthor());
                    holder.textBookISBN.setText("ISBN: " + bookDetail.getIsbn());
                    
                    // Show stock info
                    if (bookDetail.getStock() > 0) {
                        holder.textBookStock.setText("In stock (" + bookDetail.getStock() + " available)");
                        holder.textBookStock.setTextColor(context.getResources().getColor(R.color.status_accepted));
                    } else {
                        holder.textBookStock.setText("Out of stock");
                        holder.textBookStock.setTextColor(context.getResources().getColor(R.color.status_cancel));
                    }
                } else {
                    Log.e(TAG, "Failed to load book details for ID: " + bookId);
                }
            }
            
            @Override
            public void onFailure(Call<BookDetailResponse> call, Throwable t) {
                Log.e(TAG, "Error loading book details for ID: " + bookId, t);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return orderBooks.size();
    }
    
    public void setOrderBooks(List<OrderBookDetail> orderBooks) {
        this.orderBooks = orderBooks != null ? orderBooks : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageBookCover;
        TextView textBookTitle, textBookAuthor, textBookISBN, textBookPrice, textQuantity, textTotalBookPrice, textBookStock;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageBookCover = itemView.findViewById(R.id.imageBookCover);
            textBookTitle = itemView.findViewById(R.id.textBookTitle);
            textBookAuthor = itemView.findViewById(R.id.textBookAuthor);
            textBookISBN = itemView.findViewById(R.id.textBookISBN);
            textBookPrice = itemView.findViewById(R.id.textBookPrice);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textTotalBookPrice = itemView.findViewById(R.id.textTotalBookPrice);
            textBookStock = itemView.findViewById(R.id.textBookStock);
        }
    }
}
