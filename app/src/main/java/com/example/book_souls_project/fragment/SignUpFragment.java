package com.example.book_souls_project.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.book_souls_project.R;
import com.example.book_souls_project.api.ApiClient;
import com.example.book_souls_project.api.service.AuthenticationService;
import com.example.book_souls_project.api.types.auth.RegisterRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpFragment extends Fragment {
    private TextInputEditText editTextFullName, editTextEmail, editTextPhone, editTextPassword, editTextConfirmPassword;
    private TextInputEditText editTextStreet, editTextWard, editTextDistrict, editTextCity, editTextCountry;
    private MaterialButton buttonSignUp;
    private View textLogIn;
    private AuthenticationService authenticationService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.authentication_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        authenticationService = ApiClient.getInstance(requireContext()).getRetrofit().create(AuthenticationService.class);
        buttonSignUp.setOnClickListener(v -> handleSignUp());
        textLogIn.setOnClickListener(v -> NavHostFragment.findNavController(SignUpFragment.this).navigate(R.id.action_SignupFragment_to_LoginFragment));
    }

    private void initViews(View view) {
        editTextFullName = view.findViewById(R.id.editTextFullName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        editTextPassword = view.findViewById(R.id.editTextPassword);
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword);
        buttonSignUp = view.findViewById(R.id.buttonSignUp);
        textLogIn = view.findViewById(R.id.textLogIn);
        // Address fields (add these to your layout if missing)
        editTextStreet = view.findViewById(R.id.editTextStreet);
        editTextWard = view.findViewById(R.id.editTextWard);
        editTextDistrict = view.findViewById(R.id.editTextDistrict);
        editTextCity = view.findViewById(R.id.editTextCity);
        editTextCountry = view.findViewById(R.id.editTextCountry);
    }

    private void handleSignUp() {
        String fullName = getText(editTextFullName);
        String email = getText(editTextEmail);
        String phone = getText(editTextPhone);
        String password = getText(editTextPassword);
        String confirmPassword = getText(editTextConfirmPassword);
        String street = getText(editTextStreet);
        String ward = getText(editTextWard);
        String district = getText(editTextDistrict);
        String city = getText(editTextCity);
        String country = getText(editTextCountry);

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)
                || TextUtils.isEmpty(street) || TextUtils.isEmpty(ward) || TextUtils.isEmpty(district)
                || TextUtils.isEmpty(city) || TextUtils.isEmpty(country)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        RegisterRequest.Address address = new RegisterRequest.Address();
        address.setStreet(street);
        address.setWard(ward);
        address.setDistrict(district);
        address.setCity(city);
        address.setCountry(country);

        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setConfirmPassword(confirmPassword);
        request.setFullName(fullName);
        request.setPhoneNumber(phone);
        request.setAddress(address);

        buttonSignUp.setEnabled(false);
        buttonSignUp.setText("Account is being created");
        authenticationService.register(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                buttonSignUp.setEnabled(true);
                buttonSignUp.setText("Account is being created");
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Registration successful! Please login.", Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(SignUpFragment.this).navigate(R.id.action_SignupFragment_to_LoginFragment);
                } else {
                    buttonSignUp.setText("Create Account");
                    Toast.makeText(getContext(), "Registration failed: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                buttonSignUp.setEnabled(true);
                buttonSignUp.setText("Create Account");
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getText(TextInputEditText editText) {
        return editText != null && editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
