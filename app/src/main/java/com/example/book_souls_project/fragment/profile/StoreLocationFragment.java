package com.example.book_souls_project.fragment.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.book_souls_project.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class StoreLocationFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView textStoreAddress;
    
    // Địa chỉ mặc định: Lưu Hữu Phước Tân Lập, Đông Hoà, Dĩ An, Bình Dương, Việt Nam
    private static final double STORE_LAT = 10.8875;
    private static final double STORE_LNG = 106.7805;
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
        
        textStoreAddress = view.findViewById(R.id.textStoreAddress);
        textStoreAddress.setText(STORE_ADDRESS);
        
        // Setup toolbar navigation
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

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
        mMap.getUiSettings().setMapToolbarEnabled(true);
    }
}
