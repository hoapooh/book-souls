package com.example.book_souls_project;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.api.repository.AuthRepository;
import com.example.book_souls_project.util.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
        
        // Check login status and navigate accordingly
        checkLoginStatusAndNavigate();
        
        // Listen for destination changes to show/hide bottom navigation
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destinationId = destination.getId();
            
            // Show bottom navigation for main app fragments
            if (destinationId == R.id.navigation_home ||
                destinationId == R.id.navigation_book_search ||
                destinationId == R.id.navigation_cart ||
                destinationId == R.id.navigation_profile) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            } else {
                // Hide for login, signup, chat, and other fragments
                bottomNavigationView.setVisibility(View.GONE);
            }
        });
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
}