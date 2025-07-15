package com.example.book_souls_project.fragment.orders;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.book_souls_project.R;
import com.example.book_souls_project.adapter.OrderDetailBookAdapter;
import com.example.book_souls_project.api.ApiClient;
import com.example.book_souls_project.api.service.BookService;
import com.example.book_souls_project.api.service.OrderService;
import com.example.book_souls_project.api.service.UserService;
import com.example.book_souls_project.api.types.book.BookDetailResponse;
import com.example.book_souls_project.api.types.order.OrderDetail;
import com.example.book_souls_project.api.types.order.OrderDetailResponse;
import com.example.book_souls_project.api.types.user.UserProfile;
import com.example.book_souls_project.util.TokenManager;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailFragment extends Fragment {
    private static final String TAG = "OrderDetailFragment";
    private static final String ARG_ORDER_ID = "order_id";
    
    private String orderId;
    private OrderDetail orderDetail;
    
    // Views
    private ProgressBar progressBar;
    private View layoutOrderDetail;
    private ImageButton buttonBack;
    
    // Order Info Views
    private TextView textOrderCode, textOrderDate, textOrderStatus, textPaymentStatus, textTotalPrice;
    private CardView cardOrderStatus, cardPaymentStatus;
    private TextView textCancelReason;
    
    // Customer Info Views
    private ImageView imageCustomerAvatar;
    private TextView textCustomerName, textCustomerEmail, textCustomerPhone;
    private TextView textShippingAddress;
    
    // Books RecyclerView
    private RecyclerView recyclerViewBooks;
    private OrderDetailBookAdapter bookAdapter;
    
    // Services
    private OrderService orderService;
    private UserService userService;
    private BookService bookService;
    private TokenManager tokenManager;
    
    public static OrderDetailFragment newInstance(String orderId) {
        OrderDetailFragment fragment = new OrderDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
        }
        
        ApiClient apiClient = ApiClient.getInstance(requireContext());
        orderService = apiClient.getRetrofit().create(OrderService.class);
        userService = apiClient.getRetrofit().create(UserService.class);
        bookService = apiClient.getRetrofit().create(BookService.class);
        tokenManager = apiClient.getTokenManager();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
        setupRecyclerView();
        
        if (orderId != null) {
            loadOrderDetail();
        } else {
            Toast.makeText(getContext(), "Order ID not found", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
        }
    }
    
    private void initViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        layoutOrderDetail = view.findViewById(R.id.layoutOrderDetail);
        buttonBack = view.findViewById(R.id.buttonBack);
        
        // Order Info Views
        textOrderCode = view.findViewById(R.id.textOrderCode);
        textOrderDate = view.findViewById(R.id.textOrderDate);
        textOrderStatus = view.findViewById(R.id.textOrderStatus);
        textPaymentStatus = view.findViewById(R.id.textPaymentStatus);
        textTotalPrice = view.findViewById(R.id.textTotalPrice);
        cardOrderStatus = view.findViewById(R.id.cardOrderStatus);
        cardPaymentStatus = view.findViewById(R.id.cardPaymentStatus);
        textCancelReason = view.findViewById(R.id.textCancelReason);
        
        // Customer Info Views
        imageCustomerAvatar = view.findViewById(R.id.imageCustomerAvatar);
        textCustomerName = view.findViewById(R.id.textCustomerName);
        textCustomerEmail = view.findViewById(R.id.textCustomerEmail);
        textCustomerPhone = view.findViewById(R.id.textCustomerPhone);
        textShippingAddress = view.findViewById(R.id.textShippingAddress);
        
        // Books RecyclerView
        recyclerViewBooks = view.findViewById(R.id.recyclerViewBooks);
    }
    
    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp());
    }
    
    private void setupRecyclerView() {
        bookAdapter = new OrderDetailBookAdapter(requireContext(), bookService);
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewBooks.setAdapter(bookAdapter);
    }
    
    private void loadOrderDetail() {
        showLoading(true);
        
        orderService.getOrderById(orderId).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(Call<OrderDetailResponse> call, Response<OrderDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderDetail = response.body().getResult();
                    displayOrderInfo();
                    loadCustomerInfo();
                    bookAdapter.setOrderBooks(orderDetail.getOrderBooks());
                } else {
                    showLoading(false);
                    Log.e(TAG, "Failed to load order detail: " + response.code());
                    Toast.makeText(getContext(), "Failed to load order detail", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<OrderDetailResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading order detail", t);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void displayOrderInfo() {
        if (orderDetail == null) return;
        
        // Order code
        textOrderCode.setText("Order #" + orderDetail.getCode());
        
        // Date
        String formattedDate = formatDate(orderDetail.getCreatedAt());
        textOrderDate.setText(formattedDate);
        
        // Order Status
        textOrderStatus.setText(orderDetail.getOrderStatus());
        setOrderStatusColor(orderDetail.getOrderStatus());
        
        // Payment Status
        textPaymentStatus.setText(orderDetail.getPaymentStatus());
        setPaymentStatusColor(orderDetail.getPaymentStatus());
        
        // Total Price
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedPrice = "₫" + formatter.format(orderDetail.getTotalPrice());
        textTotalPrice.setText(formattedPrice);
        
        // Cancel Reason
        if (orderDetail.getCancelReason() != null && !orderDetail.getCancelReason().isEmpty()) {
            textCancelReason.setVisibility(View.VISIBLE);
            textCancelReason.setText("Cancel Reason: " + orderDetail.getCancelReason());
        } else {
            textCancelReason.setVisibility(View.GONE);
        }
    }
    
    private void loadCustomerInfo() {
        userService.getUserProfile().enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    displayCustomerInfo(response.body());
                } else {
                    Log.e(TAG, "Failed to load customer info: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading customer info", t);
            }
        });
    }
    
    private void displayCustomerInfo(UserProfile userProfile) {
        // Customer avatar
        if (userProfile.getAvatar() != null && !userProfile.getAvatar().isEmpty()) {
            Glide.with(this)
                .load(userProfile.getAvatar())
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .circleCrop()
                .into(imageCustomerAvatar);
        }
        
        // Customer info
        textCustomerName.setText(userProfile.getFullName());
        textCustomerEmail.setText(userProfile.getEmail());
        textCustomerPhone.setText(userProfile.getPhoneNumber());
        
        // Shipping address
        if (userProfile.getAddress() != null) {
            String address = String.format("%s, %s, %s, %s, %s",
                userProfile.getAddress().getStreet(),
                userProfile.getAddress().getWard(),
                userProfile.getAddress().getDistrict(),
                userProfile.getAddress().getCity(),
                userProfile.getAddress().getCountry());
            textShippingAddress.setText(address);
        }
    }
    
    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }
    
    private void setOrderStatusColor(String status) {
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
            case "completed":
                colorResId = R.color.status_completed;
                break;
            default:
                colorResId = R.color.status_pending;
                break;
        }
        cardOrderStatus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), colorResId));
    }
    
    private void setPaymentStatusColor(String status) {
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
        cardPaymentStatus.setCardBackgroundColor(ContextCompat.getColor(requireContext(), colorResId));
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        layoutOrderDetail.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
