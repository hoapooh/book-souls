package com.example.book_souls_project.fragment.profile;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.book_souls_project.R;

public class ProfileFragment extends Fragment {

    private ProfileViewModel mViewModel;
    private LinearLayout layoutMyOrders;
    private LinearLayout layoutWishlist;
    private LinearLayout layoutReadingHistory;
    private LinearLayout layoutStoreLocation;
    private LinearLayout layoutSettings;
    private LinearLayout layoutSupport;
    private LinearLayout layoutLogout;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        
        initViews(view);
        setupClickListeners();
    }

    private void initViews(View view) {
        layoutMyOrders = view.findViewById(R.id.layoutMyOrders);
        layoutWishlist = view.findViewById(R.id.layoutWishlist);
        layoutReadingHistory = view.findViewById(R.id.layoutReadingHistory);
        layoutStoreLocation = view.findViewById(R.id.layoutStoreLocation);
        layoutSettings = view.findViewById(R.id.layoutSettings);
        layoutSupport = view.findViewById(R.id.layoutSupport);
        layoutLogout = view.findViewById(R.id.layoutLogout);
    }

    private void setupClickListeners() {
        layoutMyOrders.setOnClickListener(v -> {
            // TODO: Navigate to My Orders
        });

        layoutWishlist.setOnClickListener(v -> {
            // TODO: Navigate to Wishlist
        });

        layoutReadingHistory.setOnClickListener(v -> {
            // TODO: Navigate to Reading History
        });

        layoutStoreLocation.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_storeLocationFragment);
        });

        layoutSettings.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_settingsFragment);
        });

        layoutSupport.setOnClickListener(v -> {
            // TODO: Navigate to Help & Support
        });

        layoutLogout.setOnClickListener(v -> {
            // TODO: Implement logout functionality
        });
    }
}