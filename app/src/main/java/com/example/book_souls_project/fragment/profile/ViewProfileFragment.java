package com.example.book_souls_project.fragment.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.book_souls_project.R;
import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.api.repository.UserRepository;
import com.example.book_souls_project.api.types.user.UserProfile;
import com.example.book_souls_project.util.ImageUtils;
import com.google.android.material.button.MaterialButton;

public class ViewProfileFragment extends Fragment {

    // UI Components
    private ImageView imageProfilePicture;
    private TextView textFullName;
    private TextView textEmail;
    private TextView textPhoneNumber;
    private TextView textGender;
    private TextView textUserRole;
    private TextView textAddressStreet;
    private TextView textAddressWard;
    private TextView textAddressDistrict;
    private TextView textAddressCity;
    private TextView textAddressCountry;
    private MaterialButton buttonEditProfile;
    private LinearLayout layoutLoading;

    // Repository
    private UserRepository userRepository;

    public static ViewProfileFragment newInstance() {
        return new ViewProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Initialize repository
        userRepository = ApiRepository.getInstance(requireContext()).getUserRepository();
        return inflater.inflate(R.layout.fragment_view_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
        setupToolbar(view);
        loadUserProfile();
    }

    private void setupToolbar(View view) {
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void initViews(View view) {
        imageProfilePicture = view.findViewById(R.id.imageProfilePicture);
        textFullName = view.findViewById(R.id.textFullName);
        textEmail = view.findViewById(R.id.textEmail);
        textPhoneNumber = view.findViewById(R.id.textPhoneNumber);
        textGender = view.findViewById(R.id.textGender);
        textUserRole = view.findViewById(R.id.textUserRole);
        textAddressStreet = view.findViewById(R.id.textAddressStreet);
        textAddressWard = view.findViewById(R.id.textAddressWard);
        textAddressDistrict = view.findViewById(R.id.textAddressDistrict);
        textAddressCity = view.findViewById(R.id.textAddressCity);
        textAddressCountry = view.findViewById(R.id.textAddressCountry);
        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        layoutLoading = view.findViewById(R.id.layoutLoading);
    }

    private void setupClickListeners() {
        buttonEditProfile.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_viewProfileFragment_to_editProfileFragment);
        });
    }

    private void loadUserProfile() {
        showLoading(true);
        
        userRepository.getUserProfile(new UserRepository.UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                showLoading(false);
                populateUserData(userProfile);
            }

            @Override
            public void onUserProfileUpdated(UserProfile userProfile) {
                // Not used in this context
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(getContext(), "Failed to load profile: " + error, Toast.LENGTH_SHORT).show();
                
                // Show error state or navigate back
                requireActivity().onBackPressed();
            }

            @Override
            public void onLoading() {
                showLoading(true);
            }
        });
    }

    private void populateUserData(UserProfile userProfile) {
        if (userProfile == null) {
            Toast.makeText(getContext(), "No profile data available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Basic Information
        textFullName.setText(userProfile.getFullName() != null ? userProfile.getFullName() : "Not provided");
        textEmail.setText(userProfile.getEmail() != null ? userProfile.getEmail() : "Not provided");
        textPhoneNumber.setText(userProfile.getPhoneNumber() != null ? userProfile.getPhoneNumber() : "Not provided");
        textGender.setText(userProfile.getGender() != null ? userProfile.getGender() : "Not specified");
        textUserRole.setText(userProfile.getRole() != null ? userProfile.getRole() : "User");

        // Address Information
        if (userProfile.getAddress() != null) {
            textAddressStreet.setText(userProfile.getAddress().getStreet() != null ? 
                userProfile.getAddress().getStreet() : "Not provided");
            textAddressWard.setText(userProfile.getAddress().getWard() != null ? 
                userProfile.getAddress().getWard() : "N/A");
            textAddressDistrict.setText(userProfile.getAddress().getDistrict() != null ? 
                userProfile.getAddress().getDistrict() : "N/A");
            textAddressCity.setText(userProfile.getAddress().getCity() != null ? 
                userProfile.getAddress().getCity() : "N/A");
            textAddressCountry.setText(userProfile.getAddress().getCountry() != null ? 
                userProfile.getAddress().getCountry() : "N/A");
        } else {
            textAddressStreet.setText("Not provided");
            textAddressWard.setText("N/A");
            textAddressDistrict.setText("N/A");
            textAddressCity.setText("N/A");
            textAddressCountry.setText("N/A");
        }

        // Load avatar using simple loader for better compatibility
        ImageUtils.loadProfileImageSimple(requireContext(), userProfile.getAvatar(), imageProfilePicture);
    }

    private void showLoading(boolean isLoading) {
        if (layoutLoading != null) {
            layoutLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (buttonEditProfile != null) {
            buttonEditProfile.setEnabled(!isLoading);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload profile data when returning from edit screen
        loadUserProfile();
    }
}
