package com.example.book_souls_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.book_souls_project.R;
import com.example.book_souls_project.util.CartManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {
    
    private List<CartManager.CartItem> cartItems = new ArrayList<>();
    private OnCartItemActionListener listener;
    
    public interface OnCartItemActionListener {
        void onItemSelectionChanged(String bookId, boolean isSelected);
        void onQuantityChanged(String bookId, int newQuantity);
        void onItemRemoved(String bookId);
    }
    
    public CartItemAdapter(OnCartItemActionListener listener) {
        this.listener = listener;
    }
    
    public void updateCartItems(List<CartManager.CartItem> newItems) {
        this.cartItems = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartItemViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartManager.CartItem item = cartItems.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return cartItems.size();
    }
    
    class CartItemViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkboxSelect;
        private ImageView imageBookCover;
        private TextView textBookTitle;
        private TextView textBookAuthor;
        private TextView textBookPrice;
        private TextView textQuantity;
        private ImageButton buttonDecrease;
        private ImageButton buttonIncrease;
        private ImageButton buttonRemove;
        private TextView textTotalPrice;
        
        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
            imageBookCover = itemView.findViewById(R.id.imageBookCover);
            textBookTitle = itemView.findViewById(R.id.textBookTitle);
            textBookAuthor = itemView.findViewById(R.id.textBookAuthor);
            textBookPrice = itemView.findViewById(R.id.textBookPrice);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonRemove = itemView.findViewById(R.id.buttonRemove);
            textTotalPrice = itemView.findViewById(R.id.textTotalPrice);
        }
        
        public void bind(CartManager.CartItem item) {
            // Set checkbox state
            checkboxSelect.setOnCheckedChangeListener(null); // Remove listener temporarily
            checkboxSelect.setChecked(item.isSelected());
            checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onItemSelectionChanged(item.getBook().getId(), isChecked);
                }
            });
            
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
            
            // Format and set prices
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            textBookPrice.setText(currencyFormat.format(item.getBook().getPrice()));
            textTotalPrice.setText(currencyFormat.format(item.getTotalPrice()));
            
            // Set quantity
            textQuantity.setText(String.valueOf(item.getQuantity()));
            
            // Set up quantity controls
            buttonDecrease.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() - 1;
                if (newQuantity > 0 && listener != null) {
                    listener.onQuantityChanged(item.getBook().getId(), newQuantity);
                }
            });
            
            buttonIncrease.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                if (listener != null) {
                    listener.onQuantityChanged(item.getBook().getId(), newQuantity);
                }
            });
            
            // Set up remove button
            buttonRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemRemoved(item.getBook().getId());
                }
            });
            
            // Enable/disable decrease button
            buttonDecrease.setEnabled(item.getQuantity() > 1);
            buttonDecrease.setAlpha(item.getQuantity() > 1 ? 1.0f : 0.5f);
        }
    }
}
