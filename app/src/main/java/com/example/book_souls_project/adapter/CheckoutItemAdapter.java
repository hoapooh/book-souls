package com.example.book_souls_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.book_souls_project.R;
import com.example.book_souls_project.util.CartManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.CheckoutItemViewHolder> {
    
    private List<CartManager.CartItem> checkoutItems;
    
    public CheckoutItemAdapter(List<CartManager.CartItem> checkoutItems) {
        this.checkoutItems = checkoutItems;
    }
    
    @NonNull
    @Override
    public CheckoutItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout, parent, false);
        return new CheckoutItemViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CheckoutItemViewHolder holder, int position) {
        CartManager.CartItem item = checkoutItems.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return checkoutItems.size();
    }
    
    static class CheckoutItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageBookCover;
        private TextView textBookTitle;
        private TextView textBookAuthor;
        private TextView textBookPrice;
        private TextView textQuantity;
        private TextView textTotalPrice;
        
        public CheckoutItemViewHolder(@NonNull View itemView) {
            super(itemView);
            
            imageBookCover = itemView.findViewById(R.id.imageBookCover);
            textBookTitle = itemView.findViewById(R.id.textBookTitle);
            textBookAuthor = itemView.findViewById(R.id.textBookAuthor);
            textBookPrice = itemView.findViewById(R.id.textBookPrice);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textTotalPrice = itemView.findViewById(R.id.textTotalPrice);
        }
        
        public void bind(CartManager.CartItem item) {
            // Load book cover
            Glide.with(itemView.getContext())
                    .load(item.getBook().getImageUrl())
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .centerCrop()
                    .into(imageBookCover);
            
            // Set book information
            textBookTitle.setText(item.getBook().getTitle());
            textBookAuthor.setText(item.getBook().getAuthor());
            textQuantity.setText("Qty: " + item.getQuantity());
            
            // Format and set prices
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            textBookPrice.setText(currencyFormat.format(item.getBook().getPrice()));
            textTotalPrice.setText(currencyFormat.format(item.getTotalPrice()));
        }
    }
}
