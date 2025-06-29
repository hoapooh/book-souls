package com.example.book_souls_project;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        
        // Set up bottom navigation with navigation controller
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        
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
                // Hide for login and signup fragments
                bottomNavigationView.setVisibility(View.GONE);
            }
        });
    }
}