package com.example.book_souls_project.fragment.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.book_souls_project.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileFragment extends Fragment {

    private ImageView imageProfilePicture;
    private TextInputEditText editTextFullName;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPhone;
    private TextInputEditText editTextAddress;
    private MaterialButton buttonSave;
    private MaterialButton buttonChangePhoto;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public static EditProfileFragment newInstance() {
        return new EditProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            imageProfilePicture.setImageURI(imageUri);
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        loadCurrentUserData();
        setupClickListeners();
        setupToolbar(view);
    }

    private void setupToolbar(View view) {
        androidx.appcompat.widget.Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void initViews(View view) {
        imageProfilePicture = view.findViewById(R.id.imageProfilePicture);
        editTextFullName = view.findViewById(R.id.editTextFullName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        editTextAddress = view.findViewById(R.id.editTextAddress);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonChangePhoto = view.findViewById(R.id.buttonChangePhoto);
    }

    private void loadCurrentUserData() {
        // TODO: Load current user data from database/preferences
        editTextFullName.setText("John Doe");
        editTextEmail.setText("john.doe@email.com");
        editTextPhone.setText("+84 123 456 789");
        editTextAddress.setText("123 Main Street, Ho Chi Minh City");
    }

    private void setupClickListeners() {
        buttonChangePhoto.setOnClickListener(v -> openImagePicker());
        
        buttonSave.setOnClickListener(v -> saveProfile());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void saveProfile() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();

        if (fullName.isEmpty()) {
            editTextFullName.setError("Full name is required");
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            return;
        }

        // TODO: Implement actual save logic
        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        
        // Navigate back
        requireActivity().onBackPressed();
    }
}
