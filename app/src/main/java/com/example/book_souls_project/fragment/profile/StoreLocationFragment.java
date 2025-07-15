package com.example.book_souls_project.fragment.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.example.book_souls_project.R;
import com.example.book_souls_project.util.MapTestHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class StoreLocationFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "StoreLocationFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    
    private GoogleMap mMap;
    private TextView textStoreAddress;
    private MaterialButton btnGetDirections;
    private MaterialButton btnCall;
    
    // Địa chỉ mặc định: Lưu Hữu Phước Tân Lập, Đông Hoà, Dĩ An, Bình Dương, Việt Nam
    private static final double STORE_LAT = 10.875165839150132;
    private static final double STORE_LNG = 106.80071170350308;
    private static final String STORE_ADDRESS = "Lưu Hữu Phước Tân Lập, Đông Hoà, Dĩ An, Bình Dương, Việt Nam";

    public static StoreLocationFragment newInstance() {
        return new StoreLocationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_store_location, container, false);
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
        
        // Test Google Maps setup
        MapTestHelper.validateGoogleMapsSetup(requireContext());
        MapTestHelper.logMapSetupInfo();
        
        // Initialize views
        textStoreAddress = view.findViewById(R.id.textStoreAddress);
        btnGetDirections = view.findViewById(R.id.btnGetDirections);
        btnCall = view.findViewById(R.id.btnCall);
        
        textStoreAddress.setText(STORE_ADDRESS);
        
        // Setup button click listeners
        setupButtonListeners();
        
        // Request location permissions
        requestLocationPermissions();
        
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "SupportMapFragment is null");
            Toast.makeText(getContext(), "Failed to load map", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "Map is ready");
        mMap = googleMap;

        try {
            // Add a marker at the store location
            LatLng storeLocation = new LatLng(STORE_LAT, STORE_LNG);
            mMap.addMarker(new MarkerOptions()
                    .position(storeLocation)
                    .title("Book Souls Store")
                    .snippet(STORE_ADDRESS));
            
            // Move the camera to the store location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLocation, 15));
            
            // Enable zoom controls
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false); // Disable map toolbar to reduce lag
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.getUiSettings().setTiltGesturesEnabled(true);
            
            // Optimize map rendering
            mMap.setBuildingsEnabled(false); // Disable 3D buildings to improve performance
            mMap.setTrafficEnabled(false); // Disable traffic layer to improve performance
            
            // Enable my location if permission is granted
            enableMyLocationIfPermitted();
            
            Log.d(TAG, "Map setup completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up map", e);
            Toast.makeText(getContext(), "Error setting up map: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    
    private void enableMyLocationIfPermitted() {
        if (mMap != null && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(true);
                Log.d(TAG, "My location enabled");
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception when enabling my location", e);
            }
        }
    }
    
    private void setupButtonListeners() {
        btnGetDirections.setOnClickListener(v -> openDirections());
        btnCall.setOnClickListener(v -> makePhoneCall());
    }
    
    private void openDirections() {
        try {
            // Create a URI for Google Maps directions
            String uri = String.format("geo:%f,%f?q=%f,%f(%s)", 
                    STORE_LAT, STORE_LNG, STORE_LAT, STORE_LNG, "Book Souls Store");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Fallback to web browser
                String webUri = String.format("https://www.google.com/maps/dir/?api=1&destination=%f,%f", 
                        STORE_LAT, STORE_LNG);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
                startActivity(webIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening directions", e);
            Toast.makeText(getContext(), "Unable to open directions", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void makePhoneCall() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0123456789"));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error making phone call", e);
            Toast.makeText(getContext(), "Unable to make phone call", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocationIfPermitted();
                Log.d(TAG, "Location permission granted");
            } else {
                Log.d(TAG, "Location permission denied");
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
