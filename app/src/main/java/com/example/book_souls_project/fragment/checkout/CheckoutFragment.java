package com.example.book_souls_project.fragment.checkout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_souls_project.R;
import com.example.book_souls_project.adapter.CheckoutItemAdapter;
import com.example.book_souls_project.api.ApiClient;
import com.example.book_souls_project.api.service.OrderService;
import com.example.book_souls_project.api.service.UserService;
import com.example.book_souls_project.api.types.order.CreateOrderRequest;
import com.example.book_souls_project.api.types.user.UserAddress;
import com.example.book_souls_project.api.types.user.UserProfile;
import com.example.book_souls_project.util.CartManager;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private static final String TAG = "CheckoutFragment";
    
    // UI Components
    private TextView textDeliveryName;
    private TextView textDeliveryAddress;
    private TextView textDeliveryPhone;
    private RecyclerView recyclerViewCheckoutItems;
    private TextView textOrderSummary;
    private TextView textSubtotal;
    private TextView textTotal;
    private MaterialButton buttonPlaceOrder;
    private MaterialButton buttonBackToCart;
    
    // Data
    private CartManager cartManager;
    private CheckoutItemAdapter adapter;
    private List<CartManager.CartItem> selectedItems;
    private UserProfile userProfile;

    // API Services
    private UserService userService;
    private OrderService orderService;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        initServices();
        cartManager = new CartManager(requireContext());
        
        // Get selected items from cart
        selectedItems = cartManager.getSelectedItems();
        
        if (selectedItems.isEmpty()) {
            // If no items selected, show error and go back
            Toast.makeText(getContext(), "No items selected for checkout", Toast.LENGTH_LONG).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }
        
        setupRecyclerView();
        setupClickListeners();
        loadUserProfile();
        updateOrderSummary();
    }

    private void initServices() {
        ApiClient apiClient = ApiClient.getInstance(requireContext());
        userService = apiClient.getRetrofit().create(UserService.class);
        orderService = apiClient.getRetrofit().create(OrderService.class);
    }
    
    private void initViews(View view) {
        textDeliveryName = view.findViewById(R.id.textDeliveryName);
        textDeliveryAddress = view.findViewById(R.id.textDeliveryAddress);
        textDeliveryPhone = view.findViewById(R.id.textDeliveryPhone);
        recyclerViewCheckoutItems = view.findViewById(R.id.recyclerViewCheckoutItems);
        textOrderSummary = view.findViewById(R.id.textOrderSummary);
        textSubtotal = view.findViewById(R.id.textSubtotal);
        textTotal = view.findViewById(R.id.textTotal);
        buttonPlaceOrder = view.findViewById(R.id.buttonPlaceOrder);
        buttonBackToCart = view.findViewById(R.id.buttonBackToCart);
        
        // Setup back button in header
        view.findViewById(R.id.buttonBack).setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
    }

    private void loadUserProfile() {
        userService.getUserProfile().enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userProfile = response.body();
                    updateDeliveryInfo();
                } else {
                    Log.e(TAG, "Failed to load user profile: " + response.code());
                    Toast.makeText(getContext(), "Failed to load delivery address", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                Log.e(TAG, "Error loading user profile", t);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDeliveryInfo() {
        if (userProfile != null) {
            textDeliveryName.setText(userProfile.getFullName());
            
            // Format address
            String address = "";
            if (userProfile.getAddress() != null) {
                UserAddress addr = userProfile.getAddress();
                StringBuilder sb = new StringBuilder();
                if (addr.getStreet() != null && !addr.getStreet().isEmpty()) {
                    sb.append(addr.getStreet());
                }
                if (addr.getWard() != null && !addr.getWard().isEmpty()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(addr.getWard());
                }
                if (addr.getDistrict() != null && !addr.getDistrict().isEmpty()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(addr.getDistrict());
                }
                if (addr.getCity() != null && !addr.getCity().isEmpty()) {
                    if (sb.length() > 0) sb.append("\n");
                    sb.append(addr.getCity());
                }
                if (addr.getCountry() != null && !addr.getCountry().isEmpty()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(addr.getCountry());
                }
                address = sb.toString();
            }
            
            textDeliveryAddress.setText(address.isEmpty() ? "No address provided" : address);
            textDeliveryPhone.setText(userProfile.getPhoneNumber() != null ? 
                userProfile.getPhoneNumber() : "No phone number");
        }
    }
    
    private void setupRecyclerView() {
        adapter = new CheckoutItemAdapter(selectedItems);
        recyclerViewCheckoutItems.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCheckoutItems.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        buttonBackToCart.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
        
        buttonPlaceOrder.setOnClickListener(v -> {
            createOrder();
        });
    }
    
    private void updateOrderSummary() {
        int itemCount = 0;
        int subtotal = 0;
        
        for (CartManager.CartItem item : selectedItems) {
            itemCount += item.getQuantity();
            subtotal += item.getTotalPrice();
        }
        
        // Format currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        // Update UI
        textOrderSummary.setText("Order Summary (" + itemCount + " items)");
        textSubtotal.setText(currencyFormat.format(subtotal));
        textTotal.setText(currencyFormat.format(subtotal)); // Total is now same as subtotal since no tax
    }

    private void createOrder() {
        if (userProfile == null) {
            Toast.makeText(getContext(), "Please wait for profile to load", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare order request
        List<CreateOrderRequest.OrderBook> orderBooks = new ArrayList<>();
        for (CartManager.CartItem item : selectedItems) {
            orderBooks.add(new CreateOrderRequest.OrderBook(
                item.getBook().getId(),
                item.getQuantity()
            ));
        }

        CreateOrderRequest request = new CreateOrderRequest(userProfile.getId(), orderBooks);

        // Show loading
        buttonPlaceOrder.setEnabled(false);
        buttonPlaceOrder.setText("Creating Order...");

        orderService.createOrder(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                buttonPlaceOrder.setEnabled(true);
                buttonPlaceOrder.setText("Place Order");

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String paymentUrl = response.body().string();
                        if (!paymentUrl.isEmpty()) {
                            // Open payment URL in browser
                            openPaymentUrl(paymentUrl);
                            
                            // Remove selected items from cart
                            cartManager.removeSelectedItems();
                            
                            // Navigate back to home
                            Navigation.findNavController(requireView()).navigate(R.id.navigation_home);
                        } else {
                            Toast.makeText(getContext(), "Order created but no payment URL received", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading response body", e);
                        Toast.makeText(getContext(), "Error processing response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to create order: " + response.code());
                    Toast.makeText(getContext(), "Failed to create order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error creating order", t);
                buttonPlaceOrder.setEnabled(true);
                buttonPlaceOrder.setText("Place Order");
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openPaymentUrl(String paymentUrl) {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
            startActivity(browserIntent);
            
            Toast.makeText(getContext(), "Redirecting to payment...", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error opening payment URL", e);
            Toast.makeText(getContext(), "Unable to open payment page", Toast.LENGTH_SHORT).show();
        }
    }
}
