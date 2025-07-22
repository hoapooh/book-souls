package com.example.book_souls_project.fragment.orders;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.book_souls_project.R;
import com.example.book_souls_project.adapter.OrderAdapter;
import com.example.book_souls_project.api.ApiClient;
import com.example.book_souls_project.api.service.OrderService;
import com.example.book_souls_project.api.types.order.Order;
import com.example.book_souls_project.api.types.order.OrderResponse;
import com.example.book_souls_project.util.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersFragment extends Fragment {
    private static final String TAG = "OrdersFragment";
    
    private RecyclerView recyclerViewOrders;
    private ProgressBar progressBar;
    private View layoutEmptyState;
    private Spinner spinnerOrderStatus, spinnerPaymentStatus;
    private MaterialButton buttonApplyFilter;
    private ImageButton buttonBack;
    
    // Pagination controls
    private View paginationCard;
    private MaterialButton buttonPreviousPage, buttonNextPage;
    private TextView textPageInfo;
    
    private OrderAdapter orderAdapter;
    private List<Order> orders = new ArrayList<>();
    
    private OrderService orderService;
    private TokenManager tokenManager;
    
    private String currentCustomerId;
    private String selectedOrderStatus = "";
    private String selectedPaymentStatus = "";
    private int currentPage = 1;
    private int totalPages = 1;
    private final int PAGE_SIZE = 10;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApiClient apiClient = ApiClient.getInstance(requireContext());
        orderService = apiClient.getRetrofit().create(OrderService.class);
        tokenManager = apiClient.getTokenManager();
        
        // Get customer ID from token manager
        currentCustomerId = tokenManager.getUserId();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews(view);
        setupRecyclerView();
        setupSpinners();
        setupClickListeners();
        
        // Load orders directly since we have customer ID from token manager
        if (currentCustomerId != null) {
            loadOrders();
        } else {
            Navigation.findNavController(view).navigateUp();
        }
    }

    private void initViews(View view) {
        recyclerViewOrders = view.findViewById(R.id.recyclerViewOrders);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        spinnerOrderStatus = view.findViewById(R.id.spinnerOrderStatus);
        spinnerPaymentStatus = view.findViewById(R.id.spinnerPaymentStatus);
        buttonApplyFilter = view.findViewById(R.id.buttonApplyFilter);
        buttonBack = view.findViewById(R.id.buttonBack);
        
        // Pagination controls
        paginationCard = view.findViewById(R.id.paginationCard);
        buttonPreviousPage = view.findViewById(R.id.buttonPreviousPage);
        buttonNextPage = view.findViewById(R.id.buttonNextPage);
        textPageInfo = view.findViewById(R.id.textPageInfo);
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(getContext(), orders);
        orderAdapter.setOnOrderClickListener(order -> {
            // Navigate to order detail
            Bundle args = new Bundle();
            args.putString("order_id", order.getId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_ordersFragment_to_orderDetailFragment, args);
        });
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void setupSpinners() {
        // Order Status Spinner
        String[] orderStatuses = {"All", "Pending", "Accepted", "Cancel", "Shipping"};
        ArrayAdapter<String> orderStatusAdapter = new ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_dropdown_item, orderStatuses);
        spinnerOrderStatus.setAdapter(orderStatusAdapter);
        
        spinnerOrderStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedOrderStatus = position == 0 ? "" : orderStatuses[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Payment Status Spinner
        String[] paymentStatuses = {"All", "None", "Paid", "Refund"};
        ArrayAdapter<String> paymentStatusAdapter = new ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_dropdown_item, paymentStatuses);
        spinnerPaymentStatus.setAdapter(paymentStatusAdapter);
        
        spinnerPaymentStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPaymentStatus = position == 0 ? "" : paymentStatuses[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp());
        
        buttonApplyFilter.setOnClickListener(v -> {
            currentPage = 1;
            loadOrders();
        });
        
        // Pagination listeners
        buttonPreviousPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                loadOrders();
            }
        });
        
        buttonNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadOrders();
            }
        });
    }

    private void loadOrders() {
        if (currentCustomerId == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        orderService.getOrders(
            currentCustomerId,
            selectedOrderStatus.isEmpty() ? null : selectedOrderStatus,
            selectedPaymentStatus.isEmpty() ? null : selectedPaymentStatus,
            currentPage,
            PAGE_SIZE
        ).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    OrderResponse orderResponse = response.body();
                    if (orderResponse.getResult() != null) {
                        orders.clear();
                        orders.addAll(orderResponse.getResult().getItems());
                        orderAdapter.updateOrders(orders);
                        
                        // Update pagination info
                        int totalCount = orderResponse.getResult().getTotalCount();
                        totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
                        updatePaginationUI();
                        
                        // Show empty state if no orders
                        if (orders.isEmpty()) {
                            showEmptyState(true);
                        } else {
                            showEmptyState(false);
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to load orders: " + response.code());
                    Toast.makeText(getContext(), "Failed to load orders", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                Log.e(TAG, "Error loading orders", t);
                showLoading(false);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewOrders.setVisibility(show ? View.GONE : View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean show) {
        layoutEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewOrders.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void updatePaginationUI() {
        // Show pagination only if there are multiple pages
        if (totalPages > 1) {
            paginationCard.setVisibility(View.VISIBLE);
            
            // Update page info text
            textPageInfo.setText("Page " + currentPage + " of " + totalPages);
            
            // Enable/disable buttons based on current page
            buttonPreviousPage.setEnabled(currentPage > 1);
            buttonNextPage.setEnabled(currentPage < totalPages);
            
            // Update button appearance
            buttonPreviousPage.setAlpha(currentPage > 1 ? 1.0f : 0.5f);
            buttonNextPage.setAlpha(currentPage < totalPages ? 1.0f : 0.5f);
        } else {
            paginationCard.setVisibility(View.GONE);
        }
    }
}
