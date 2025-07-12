package com.example.book_souls_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_souls_project.R;
import com.example.book_souls_project.api.types.order.Order;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderBookAdapter extends RecyclerView.Adapter<OrderBookAdapter.ViewHolder> {
    private List<Order.OrderBook> orderBooks;

    public OrderBookAdapter(List<Order.OrderBook> orderBooks) {
        this.orderBooks = orderBooks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order.OrderBook orderBook = orderBooks.get(position);
        
        holder.textBookTitle.setText(orderBook.getBookTitle());
        holder.textQuantity.setText("x" + orderBook.getQuantity());
        
        // Format price
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedPrice = "₫" + formatter.format(orderBook.getBookPrice());
        holder.textBookPrice.setText(formattedPrice);
    }

    @Override
    public int getItemCount() {
        return orderBooks != null ? orderBooks.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textBookTitle, textQuantity, textBookPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textBookTitle = itemView.findViewById(R.id.textBookTitle);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textBookPrice = itemView.findViewById(R.id.textBookPrice);
        }
    }
}
