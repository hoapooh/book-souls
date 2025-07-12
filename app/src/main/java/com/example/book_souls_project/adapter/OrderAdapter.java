package com.example.book_souls_project.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_souls_project.R;
import com.example.book_souls_project.api.types.order.Order;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private List<Order> orders;
    private Context context;
    private OnOrderClickListener onOrderClickListener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
    }
    
    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.onOrderClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        
        // Set order code
        holder.textOrderCode.setText("Order #" + order.getCode());
        
        // Format and set date
        String formattedDate = formatDate(order.getCreatedAt());
        holder.textOrderDate.setText(formattedDate);
        
        // Set order status with color
        holder.textOrderStatus.setText(order.getOrderStatus());
        setOrderStatusColor(holder.textOrderStatus, order.getOrderStatus());
        
        // Set payment status with color
        holder.textPaymentStatus.setText(order.getPaymentStatus());
        setPaymentStatusColor(holder.textPaymentStatus, order.getPaymentStatus());
        
        // Format and set total price
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedPrice = "₫" + formatter.format(order.getTotalPrice());
        holder.textTotalPrice.setText(formattedPrice);
        
        // Set up books RecyclerView
        OrderBookAdapter booksAdapter = new OrderBookAdapter(order.getOrderBooks());
        holder.recyclerViewOrderBooks.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerViewOrderBooks.setAdapter(booksAdapter);
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onOrderClickListener != null) {
                onOrderClickListener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    private void setOrderStatusColor(TextView textView, String status) {
        CardView parent = (CardView) textView.getParent();
        int colorResId;
        
        switch (status.toLowerCase()) {
            case "pending":
                colorResId = R.color.status_pending;
                break;
            case "accepted":
                colorResId = R.color.status_accepted;
                break;
            case "cancel":
                colorResId = R.color.status_cancel;
                break;
            case "shipping":
                colorResId = R.color.status_shipping;
                break;
            default:
                colorResId = R.color.status_pending;
                break;
        }
        
        parent.setCardBackgroundColor(ContextCompat.getColor(context, colorResId));
    }

    private void setPaymentStatusColor(TextView textView, String status) {
        CardView parent = (CardView) textView.getParent();
        int colorResId;
        
        switch (status.toLowerCase()) {
            case "paid":
                colorResId = R.color.status_paid;
                break;
            case "none":
                colorResId = R.color.status_none;
                break;
            case "refund":
                colorResId = R.color.status_refund;
                break;
            default:
                colorResId = R.color.status_none;
                break;
        }
        
        parent.setCardBackgroundColor(ContextCompat.getColor(context, colorResId));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textOrderCode, textOrderDate, textOrderStatus, textPaymentStatus, textTotalPrice;
        RecyclerView recyclerViewOrderBooks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderCode = itemView.findViewById(R.id.textOrderCode);
            textOrderDate = itemView.findViewById(R.id.textOrderDate);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
            textPaymentStatus = itemView.findViewById(R.id.textPaymentStatus);
            textTotalPrice = itemView.findViewById(R.id.textTotalPrice);
            recyclerViewOrderBooks = itemView.findViewById(R.id.recyclerViewOrderBooks);
        }
    }
}
