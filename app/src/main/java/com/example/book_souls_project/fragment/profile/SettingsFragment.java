package com.example.book_souls_project.fragment.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.book_souls_project.R;

public class SettingsFragment extends Fragment {

    private LinearLayout layoutEditProfile;
    private LinearLayout layoutNotifications;
    private LinearLayout layoutPrivacy;
    private LinearLayout layoutLanguage;
    private LinearLayout layoutAbout;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupClickListeners();
        setupToolbar(view);
    }

    private void setupToolbar(View view) {
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void initViews(View view) {
        layoutEditProfile = view.findViewById(R.id.layoutEditProfile);
        layoutNotifications = view.findViewById(R.id.layoutNotifications);
        layoutPrivacy = view.findViewById(R.id.layoutPrivacy);
        layoutLanguage = view.findViewById(R.id.layoutLanguage);
        layoutAbout = view.findViewById(R.id.layoutAbout);
    }

    private void setupClickListeners() {
        layoutEditProfile.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_editProfileFragment);
        });

        layoutNotifications.setOnClickListener(v -> {
            // TODO: Navigate to Notifications settings
        });

        layoutPrivacy.setOnClickListener(v -> {
            // TODO: Navigate to Privacy settings
        });

        layoutLanguage.setOnClickListener(v -> {
            // TODO: Navigate to Language settings
        });

        layoutAbout.setOnClickListener(v -> {
            // TODO: Navigate to About page
        });
    }
}
