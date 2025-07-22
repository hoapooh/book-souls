package com.example.book_souls_project;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.api.repository.AuthRepository;
import com.example.book_souls_project.api.service.UserService;
import com.example.book_souls_project.api.types.user.FCMTokenRequest;
import com.example.book_souls_project.util.TokenManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    private AuthRepository authRepository;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize authentication components
        authRepository = ApiRepository.getInstance(this).getAuthRepository();
        tokenManager = new TokenManager(this);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Set up bottom navigation with navigation controller
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Set up custom bottom navigation handling
        setupBottomNavListener();

        // Check login status and navigate accordingly
        checkLoginStatusAndNavigate();

        // Create notification channel for FCM
        createFCMNotificationChannel();

        // Listen for destination changes to show/hide bottom navigation
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destinationId = destination.getId();

            // Show bottom navigation for main app fragments
            if (destinationId == R.id.navigation_home ||
                    destinationId == R.id.navigation_book_search ||
                    destinationId == R.id.navigation_cart ||
                    destinationId == R.id.navigation_profile || destinationId == R.id.storeLocationFragment) {
                bottomNavigationView.setVisibility(View.VISIBLE);

                // Sync selected item with current destination
                bottomNavigationView.setOnItemSelectedListener(null);

                if (destinationId == R.id.navigation_home) {
                    bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                } else if (destinationId == R.id.navigation_book_search) {
                    bottomNavigationView.setSelectedItemId(R.id.navigation_book_search);
                } else if (destinationId == R.id.navigation_cart) {
                    bottomNavigationView.setSelectedItemId(R.id.navigation_cart);
                } else if (destinationId == R.id.navigation_profile) {
                    bottomNavigationView.setSelectedItemId(R.id.navigation_profile);
                } else if (destinationId == R.id.storeLocationFragment) {
                    bottomNavigationView.setSelectedItemId(R.id.storeLocationFragment);
                }

                // Restore the listener
                setupBottomNavListener();
            } else {
                // Hide for login, signup, chat, and other fragments
                bottomNavigationView.setVisibility(View.GONE);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 0);
            }
        }

        // Get FCM registration token for debugging
        getFCMToken();
    }

    /**
     * Get FCM registration token for debugging
     */
    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("MainActivity", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        Log.d("MainActivity", "FCM Registration Token: " + token);
                        Toast.makeText(MainActivity.this, "FCM Token logged (check console)", Toast.LENGTH_SHORT).show();
                        
                        // Send token to server if user is logged in
                        sendFCMTokenToServer(token);
                    }
                });
    }

    /**
     * Send FCM token to server for the current logged-in user
     */
    private void sendFCMTokenToServer(String fcmToken) {
        if (authRepository.isLoggedIn()) {
            String userId = tokenManager.getUserId();
            String authToken = tokenManager.getAccessToken();
            
            if (userId != null && authToken != null) {
                UserService userService = ApiRepository.getInstance(this)
                        .getRetrofit().create(UserService.class);
                
                FCMTokenRequest request = new FCMTokenRequest(userId, fcmToken);
                
                userService.updateFCMToken("Bearer " + authToken, request)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Log.d("MainActivity", "FCM token sent to server successfully");
                                } else {
                                    Log.e("MainActivity", "Failed to send FCM token: " + response.code());
                                }
                            }
                            
                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.e("MainActivity", "Error sending FCM token", t);
                            }
                        });
            }
        } else {
            Log.d("MainActivity", "User not logged in, FCM token not sent to server");
        }
    }

    /**
     * Create notification channel for Firebase Cloud Messaging
     */
    private void createFCMNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Firebase Notifications";
            String description = "Notifications from Firebase Cloud Messaging";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("FCM_CHANNEL", name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.enableVibration(true);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d("MainActivity", "FCM notification channel created");
            }
        }
    }



    /**
     * Check login status and navigate to appropriate screen
     */
    private void checkLoginStatusAndNavigate() {
        // Post to make sure navigation is ready
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(NavController controller, androidx.navigation.NavDestination destination, Bundle arguments) {
                // Remove this listener after first navigation
                controller.removeOnDestinationChangedListener(this);

                // Check if user is logged in
                if (authRepository.isLoggedIn()) {
                    // User is logged in, navigate to home if not already there
                    if (destination.getId() != R.id.navigation_home) {
                        try {
                            controller.navigate(R.id.navigation_home);
                        } catch (Exception e) {
                            // Handle navigation error
                        }
                    }
                } else {
                    // User is not logged in, navigate to login if not already there
                    if (destination.getId() != R.id.loginFragment) {
                        try {
                            controller.navigate(R.id.loginFragment);
                        } catch (Exception e) {
                            // Handle navigation error
                        }
                    }
                }
            }
        });
    }

    /**
     * Method to handle logout from any fragment
     * Can be called from ProfileFragment or other fragments
     */
    public void handleLogout() {
        // Clear session
        authRepository.logout();

        // Navigate to login screen and clear back stack
        try {
            navController.navigate(R.id.loginFragment);
            // Clear back stack by popping everything
            navController.popBackStack(R.id.loginFragment, false);
        } catch (Exception e) {
            // Handle navigation error
        }

        // Hide bottom navigation
        bottomNavigationView.setVisibility(View.GONE);
    }

    /**
     * Method to handle successful login
     * Can be called from LoginFragment
     */
    public void handleLoginSuccess() {
        try {
            // Navigate to home
            navController.navigate(R.id.navigation_home);
            // Show bottom navigation
            bottomNavigationView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            // Handle navigation error
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handlePaymentResultNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handlePaymentResultNavigation();
    }

    /**
     * Handle navigation from payment result
     */
    private void handlePaymentResultNavigation() {
        Intent intent = getIntent();
        String navigateTo = intent.getStringExtra("navigate_to");

        if (navigateTo != null) {
            switch (navigateTo) {
                case "home":
                    try {
                        navController.navigate(R.id.navigation_home);
                        bottomNavigationView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        // Already on home or navigation error
                    }
                    break;
                case "orders":
                    try {
                        navController.navigate(R.id.navigation_profile);
                        // Small delay to ensure profile is loaded, then navigate to orders
                        bottomNavigationView.postDelayed(() -> {
                            try {
                                navController.navigate(R.id.action_profileFragment_to_ordersFragment);
                            } catch (Exception e) {
                                // Navigation error
                            }
                        }, 100);
                        bottomNavigationView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        // Navigation error
                    }
                    break;
                case "cart":
                    try {
                        navController.navigate(R.id.navigation_cart);
                        bottomNavigationView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        // Navigation error
                    }
                    break;
            }

            // Clear the intent extra to prevent repeated navigation
            intent.removeExtra("navigate_to");
        }
    }

    /**
     * Sets up the bottom navigation listener
     */
    private void setupBottomNavListener() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Get the current destination
            int currentDestId = navController.getCurrentDestination().getId();

            // If we're already at this destination, don't navigate again
            if (currentDestId == itemId) {
                return true;
            }

            // Clear backstack when navigating between major destinations
            if (itemId == R.id.navigation_home ||
                    itemId == R.id.navigation_book_search ||
                    itemId == R.id.navigation_cart ||
                    itemId == R.id.navigation_profile || itemId == R.id.storeLocationFragment) {

                // Use a Bundle to pass any necessary arguments
                Bundle args = null;

                // Special handling for home navigation to ensure it always works
                if (itemId == R.id.navigation_home) {
                    // Pop back stack to home if already there
                    boolean popped = navController.popBackStack(R.id.navigation_home, false);

                    // If we didn't pop anything, navigate to home
                    if (!popped) {
                        navController.navigate(R.id.navigation_home);
                    }
                    return true;
                }

                // Navigate to the selected destination
                navController.navigate(itemId, args,
                        new androidx.navigation.NavOptions.Builder()
                                .setPopUpTo(navController.getGraph().getStartDestination(), false)
                                .build());
                return true;
            }
            return false;
        });
    }
}