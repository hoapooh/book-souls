package com.example.book_souls_project.fragment.profile;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.book_souls_project.MainActivity;
import com.example.book_souls_project.R;
import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.api.repository.AuthRepository;
import com.example.book_souls_project.api.repository.UserRepository;
import com.example.book_souls_project.api.types.user.UserProfile;
import com.example.book_souls_project.util.ImageUtils;
import com.example.book_souls_project.util.TokenManager;

public class ProfileFragment extends Fragment {

    private ProfileViewModel mViewModel;
    private AuthRepository authRepository;
    private UserRepository userRepository;
    private TokenManager tokenManager;
    
    // UI Components
    private LinearLayout layoutViewProfile;
    private LinearLayout layoutMyOrders;
//    private LinearLayout layoutSettings;
    private LinearLayout layoutSupport;
    private LinearLayout layoutLogout;
    private LinearLayout layoutNotifications;
    
    // Profile UI Components
    private ImageView imageProfilePicture;
    private TextView textUserName;
    private TextView textUserEmail;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Initialize repositories and managers
        authRepository = ApiRepository.getInstance(requireContext()).getAuthRepository();
        userRepository = ApiRepository.getInstance(requireContext()).getUserRepository();
        tokenManager = new TokenManager(requireContext());

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Apply window insets to handle system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        
        // Check if user is logged in
        checkLoginStatus();
        
        initViews(view);
        setupClickListeners();
        loadUserProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check login status every time the fragment becomes visible
        checkLoginStatus();
        // Refresh user profile in case it was updated
        loadUserProfile();
    }

    private void initViews(View view) {
        // Profile UI components
        imageProfilePicture = view.findViewById(R.id.imageProfilePicture);
        textUserName = view.findViewById(R.id.textUserName);
        textUserEmail = view.findViewById(R.id.textUserEmail);
        
        // Menu layouts
        layoutViewProfile = view.findViewById(R.id.layoutViewProfile);
        layoutMyOrders = view.findViewById(R.id.layoutMyOrders);
//        layoutSettings = view.findViewById(R.id.layoutSettings);
        layoutSupport = view.findViewById(R.id.layoutSupport);
        layoutLogout = view.findViewById(R.id.layoutLogout);
        layoutNotifications = view.findViewById(R.id.layoutNotifications);
    }

    private void setupClickListeners() {
        layoutViewProfile.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_viewProfileFragment);
        });

        layoutMyOrders.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_ordersFragment);
        });

        /*layoutSettings.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_settingsFragment);
        });*/

        layoutSupport.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_chatFragment);
        });

        layoutNotifications.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_notificationFragment);
        });

        layoutLogout.setOnClickListener(v -> {
            showLogoutConfirmDialog();
        });
    }
    
    /**
     * Check if user is logged in, if not redirect to login
     */
    private void checkLoginStatus() {
        if (!authRepository.isLoggedIn()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        }
    }
    
    /**
     * Load user profile information from API
     */
    private void loadUserProfile() {
        if (!tokenManager.isLoggedIn()) {
            return;
        }

        userRepository.getUserProfile(new UserRepository.UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                updateUIWithUserProfile(userProfile);
            }

            @Override
            public void onUserProfileUpdated(UserProfile userProfile) {
                // Not used in this context
            }

            @Override
            public void onError(String error) {
                // Fallback to token manager data if API fails
                loadUserProfileFromToken();
                Toast.makeText(getContext(), "Could not load latest profile data", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoading() {
                // Show loading indicator if needed
            }
        });
    }

    /**
     * Update UI with user profile data
     */
    private void updateUIWithUserProfile(UserProfile userProfile) {
        if (userProfile == null) {
            loadUserProfileFromToken();
            return;
        }

        // Update UI with user info
        if (userProfile.getFullName() != null && !userProfile.getFullName().isEmpty()) {
            textUserName.setText(userProfile.getFullName());
        } else {
            textUserName.setText("User");
        }

        if (userProfile.getEmail() != null && !userProfile.getEmail().isEmpty()) {
            textUserEmail.setText(userProfile.getEmail());
        } else {
            textUserEmail.setText("user@booksouls.com");
        }

        // Load avatar using simple method like BookDetailFragment
        ImageUtils.loadProfileImageSimple(requireContext(), userProfile.getAvatar(), imageProfilePicture);
    }

    /**
     * Fallback method to load user profile from token manager
     */
    private void loadUserProfileFromToken() {
        if (tokenManager.isLoggedIn()) {
            String userName = tokenManager.getUserName();
            String userEmail = tokenManager.getUserId(); // Assuming userId is email or get email separately

            // Update UI with user info
            if (userName != null && !userName.isEmpty()) {
                textUserName.setText(userName);
            } else {
                textUserName.setText("User");
            }

            // For email, you might need to save email separately in TokenManager
            // For now, using userId or a placeholder
            if (userEmail != null && !userEmail.isEmpty()) {
                textUserEmail.setText(userEmail);
            } else {
                textUserEmail.setText("user@booksouls.com");
            }

            // Load avatar from token manager using ImageUtils
            String avatarUrl = tokenManager.getUserAvatar();
            ImageUtils.loadProfileImageSimple(requireContext(), avatarUrl, imageProfilePicture);
        }
    }
    
    /**
     * Show logout confirmation dialog
     */
    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
    
    /**
     * Perform logout operation
     */
    private void performLogout() {
        try {
            // Show success message
            Toast.makeText(requireContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
            
            // Navigate to login screen (handleLogout will clear session)
            navigateToLogin();
            
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Có lỗi xảy ra khi đăng xuất", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Navigate to login screen
     */
    private void navigateToLogin() {
        // Use MainActivity's handleLogout method for consistent navigation
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).handleLogout();
        } else {
            // Fallback navigation
            try {
                Navigation.findNavController(requireView()).navigate(R.id.loginFragment);
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        }
    }
}