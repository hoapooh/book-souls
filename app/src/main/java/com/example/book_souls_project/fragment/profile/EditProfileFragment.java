package com.example.book_souls_project.fragment.profile;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.book_souls_project.R;
import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.api.repository.UserRepository;
import com.example.book_souls_project.api.types.user.EditProfileRequest;
import com.example.book_souls_project.api.types.user.UserProfile;
import com.example.book_souls_project.util.ImageUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class EditProfileFragment extends Fragment {

    private ImageView imageProfilePicture;
    private TextInputEditText editTextFullName;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPhone;
    private TextInputEditText editTextGender;
    private TextInputEditText editTextAddressStreet;
    private TextInputEditText editTextAddressWard;
    private TextInputEditText editTextAddressDistrict;
    private TextInputEditText editTextAddressCity;
    private TextInputEditText editTextAddressCountry;
    private MaterialButton buttonSave;
    private MaterialButton buttonChangePhoto;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private UserRepository userRepository;
    private UserProfile currentUserProfile;
    private Uri selectedImageUri;
    private boolean isLoading = false;

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
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Show selected image in ImageView with better configuration
                            Glide.with(this)
                                .load(selectedImageUri)
                                .placeholder(R.drawable.ic_user)
                                .error(R.drawable.ic_user)
                                .centerCrop()
                                .into(imageProfilePicture);
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Initialize repository
        userRepository = ApiRepository.getInstance(requireContext()).getUserRepository();
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
        editTextGender = view.findViewById(R.id.editTextGender);
        editTextAddressStreet = view.findViewById(R.id.editTextAddressStreet);
        editTextAddressWard = view.findViewById(R.id.editTextAddressWard);
        editTextAddressDistrict = view.findViewById(R.id.editTextAddressDistrict);
        editTextAddressCity = view.findViewById(R.id.editTextAddressCity);
        editTextAddressCountry = view.findViewById(R.id.editTextAddressCountry);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonChangePhoto = view.findViewById(R.id.buttonChangePhoto);
    }

    private void loadCurrentUserData() {
        userRepository.getUserProfile(new UserRepository.UserProfileCallback() {
            @Override
            public void onUserProfileLoaded(UserProfile userProfile) {
                currentUserProfile = userProfile;
                populateUserData(userProfile);
            }

            @Override
            public void onUserProfileUpdated(UserProfile userProfile) {
                // Not used in this context
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Failed to load profile: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoading() {
                // Show loading indicator if needed
            }
        });
    }

    private void populateUserData(UserProfile userProfile) {
        if (userProfile == null) return;

        // Set basic info
        if (userProfile.getFullName() != null) {
            editTextFullName.setText(userProfile.getFullName());
        }
        if (userProfile.getEmail() != null) {
            editTextEmail.setText(userProfile.getEmail());
            editTextEmail.setEnabled(false); // Email usually shouldn't be editable
        }
        if (userProfile.getPhoneNumber() != null) {
            editTextPhone.setText(userProfile.getPhoneNumber());
        }
        if (userProfile.getGender() != null) {
            editTextGender.setText(userProfile.getGender());
        }

        // Set address info
        if (userProfile.getAddress() != null) {
            if (userProfile.getAddress().getStreet() != null) {
                editTextAddressStreet.setText(userProfile.getAddress().getStreet());
            }
            if (userProfile.getAddress().getWard() != null) {
                editTextAddressWard.setText(userProfile.getAddress().getWard());
            }
            if (userProfile.getAddress().getDistrict() != null) {
                editTextAddressDistrict.setText(userProfile.getAddress().getDistrict());
            }
            if (userProfile.getAddress().getCity() != null) {
                editTextAddressCity.setText(userProfile.getAddress().getCity());
            }
            if (userProfile.getAddress().getCountry() != null) {
                editTextAddressCountry.setText(userProfile.getAddress().getCountry());
            }
        }

        // Load avatar using simple loader for better compatibility
        ImageUtils.loadProfileImageSimple(requireContext(), userProfile.getAvatar(), imageProfilePicture);
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
        if (isLoading) return;

        String fullName = editTextFullName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String gender = editTextGender.getText().toString().trim();
        String addressStreet = editTextAddressStreet.getText().toString().trim();
        String addressWard = editTextAddressWard.getText().toString().trim();
        String addressDistrict = editTextAddressDistrict.getText().toString().trim();
        String addressCity = editTextAddressCity.getText().toString().trim();
        String addressCountry = editTextAddressCountry.getText().toString().trim();

        // Validate required fields
        if (fullName.isEmpty()) {
            editTextFullName.setError("Full name is required");
            editTextFullName.requestFocus();
            return;
        }

        setLoadingState(true);
        Log.d("EditProfileFragment", selectedImageUri.toString());
        if (selectedImageUri != null) {
            // Save with avatar
            saveProfileWithAvatar(fullName, phone, gender, addressStreet, addressWard,
                                addressDistrict, addressCity, addressCountry);
        } else {
            // Save without avatar - use multipart form but without avatar file
            saveProfileWithoutAvatar(fullName, phone, gender, addressStreet, addressWard, 
                                   addressDistrict, addressCity, addressCountry);
        }
    }

    private void saveProfileWithAvatar(String fullName, String phone, String gender,
                                     String addressStreet, String addressWard, String addressDistrict,
                                     String addressCity, String addressCountry) {
        try {
            // Convert URI to MultipartBody.Part
            MultipartBody.Part avatarPart = prepareFilePart("Avatar", selectedImageUri);

            userRepository.editProfileWithAvatar(fullName, phone, gender, addressStreet, addressWard,
                    addressDistrict, addressCity, addressCountry, avatarPart,
                    new UserRepository.UserProfileCallback() {
                        @Override
                        public void onUserProfileLoaded(UserProfile userProfile) {
                            // Not used in this context
                        }

                        @Override
                        public void onUserProfileUpdated(UserProfile userProfile) {
                            setLoadingState(false);
                            Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            requireActivity().onBackPressed();
                        }

                        @Override
                        public void onError(String error) {
                            setLoadingState(false);
                            Toast.makeText(getContext(), "Failed to update profile: " + error, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoading() {
                            // Loading state already handled
                        }
                    });
        } catch (Exception e) {
            setLoadingState(false);
            Toast.makeText(getContext(), "Failed to prepare image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        try {
            ContentResolver contentResolver = requireContext().getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(fileUri);
            
            // Get file name
            String fileName = getFileName(fileUri);
            if (fileName == null) {
                fileName = "avatar.jpg";
            }

            // Create temporary file
            File tempFile = new File(requireContext().getCacheDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            // Create RequestBody
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), tempFile);
            return MultipartBody.Part.createFormData(partName, fileName, requestFile);

        } catch (Exception e) {
            throw new RuntimeException("Error preparing file part", e);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;
        buttonSave.setEnabled(!loading);
        buttonChangePhoto.setEnabled(!loading);
        
        if (loading) {
            buttonSave.setText("Saving...");
        } else {
            buttonSave.setText("Save Changes");
        }
    }

    private void saveProfileWithoutAvatar(String fullName, String phone, String gender,
                                        String addressStreet, String addressWard, String addressDistrict,
                                        String addressCity, String addressCountry) {
        try {
            // Use multipart form but with empty avatar to maintain consistency with API
            userRepository.editProfileWithAvatar(fullName, phone, gender, addressStreet, addressWard,
                    addressDistrict, addressCity, addressCountry, null,
                    new UserRepository.UserProfileCallback() {
                        @Override
                        public void onUserProfileLoaded(UserProfile userProfile) {
                            // Not used in this context
                        }

                        @Override
                        public void onUserProfileUpdated(UserProfile userProfile) {
                            setLoadingState(false);
                            Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            requireActivity().onBackPressed();
                        }

                        @Override
                        public void onError(String error) {
                            setLoadingState(false);
                            Toast.makeText(getContext(), "Failed to update profile: " + error, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoading() {
                            // Loading state already handled
                        }
                    });
        } catch (Exception e) {
            setLoadingState(false);
            Toast.makeText(getContext(), "Failed to prepare request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
