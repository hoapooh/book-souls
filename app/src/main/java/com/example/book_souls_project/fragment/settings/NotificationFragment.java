package com.example.book_souls_project.fragment.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

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
import com.example.book_souls_project.adapter.NotificationAdapter;
import com.example.book_souls_project.api.ApiClient;
import com.example.book_souls_project.api.service.NotificationService;
import com.example.book_souls_project.api.types.notification.NotificationResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {
    private static final String TAG = "NotificationFragment";
    
    private RecyclerView recyclerViewNotifications;
    private LinearLayout layoutEmptyState;
    private ProgressBar progressBar;
    private ImageButton buttonBack;
    
    private NotificationAdapter notificationAdapter;
    private List<NotificationResponse> notifications = new ArrayList<>();
    
    private NotificationService notificationService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize API service
        ApiClient apiClient = ApiClient.getInstance(requireContext());
        notificationService = apiClient.getRetrofit().create(NotificationService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
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
        setupClickListeners();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews(View view) {
        recyclerViewNotifications = view.findViewById(R.id.recyclerViewNotifications);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        progressBar = view.findViewById(R.id.progressBar);
        buttonBack = view.findViewById(R.id.buttonBack);
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp());
    }

    private void setupRecyclerView() {
        notificationAdapter = new NotificationAdapter(notifications);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNotifications.setAdapter(notificationAdapter);
    }

    private void loadNotifications() {
        showLoading(true);
        notificationService.getNotifications().enqueue(new Callback<com.example.book_souls_project.api.types.notification.NotificationListResponse>() {
            @Override
            public void onResponse(Call<com.example.book_souls_project.api.types.notification.NotificationListResponse> call, Response<com.example.book_souls_project.api.types.notification.NotificationListResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    notifications.clear();
                    if (response.body().getResult().getItems() != null) {
                        notifications.addAll(response.body().getResult().getItems());
                    }
                    notificationAdapter.notifyDataSetChanged();
                    if (notifications.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                    }
                } else {
                    Log.e(TAG, "Failed to load notifications: " + response.code());
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<com.example.book_souls_project.api.types.notification.NotificationListResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error loading notifications", t);
                showEmptyState(true);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewNotifications.setVisibility(show ? View.GONE : View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean show) {
        layoutEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewNotifications.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
