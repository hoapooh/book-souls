package com.example.book_souls_project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.book_souls_project.MainActivity;
import com.example.book_souls_project.R;
import com.example.book_souls_project.api.ApiRepository;
import com.example.book_souls_project.api.repository.AuthRepository;
import com.example.book_souls_project.api.types.auth.LoginResponse;
import com.example.book_souls_project.databinding.AuthenticationLoginBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import android.widget.Toast;

public class LoginFragment extends Fragment {
    private AuthenticationLoginBinding binding;
    private TextView textSignUp;
    private EditText editTextEmail, editTextPassword;
    private MaterialButton buttonLogin;
    private AuthRepository authRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = AuthenticationLoginBinding.inflate(inflater, container, false);
        textSignUp = binding.textSignUp;

        // Initialize AuthRepository
        authRepository = ApiRepository.getInstance(requireContext()).getAuthRepository();

        initViews();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigate to signup
        textSignUp.setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_LoginFragment_to_SignupFragment);
        });

        // Set up login functionality
        setupLoginListener();

        // Handle test button click for quick navigation to home
        binding.buttonTestHome.setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_LoginFragment_to_Home);
        });
    }

    private void initViews() {
        editTextEmail = binding.editTextEmail;
        editTextPassword = binding.editTextPassword;
        buttonLogin = binding.buttonLogin;
    }

    private void setupLoginListener() {
        binding.buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            // Validate input
            if (!validateInput(email, password)) {
                return;
            }

            // Show loading state
            setLoadingState(true);

            // Perform login
            authRepository.login(email, password, new AuthRepository.AuthCallback() {
                @Override
                public void onLoginSuccess(LoginResponse response) {
                    setLoadingState(false);
                    
                    // Show success message
                    Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to home using MainActivity method
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).handleLoginSuccess();
                    } else {
                        // Fallback navigation
                        NavHostFragment.findNavController(LoginFragment.this)
                                .navigate(R.id.action_LoginFragment_to_Home);
                    }
                }

                @Override
                public void onLoginError(String error) {
                    setLoadingState(false);
                    
                    // Show error message
                    Snackbar.make(binding.getRoot(), "Lỗi đăng nhập: " + error, Snackbar.LENGTH_LONG)
                            .setAction("Thử lại", v1 -> {
                                // Clear password field for security
                                editTextPassword.setText("");
                            })
                            .show();
                }

                @Override
                public void onLoginLoading() {
                    setLoadingState(true);
                }
            });
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            editTextEmail.setError("Vui lòng nhập email");
            editTextEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Email không hợp lệ");
            editTextEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Vui lòng nhập mật khẩu");
            editTextPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void setLoadingState(boolean isLoading) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                binding.buttonLogin.setEnabled(!isLoading);
                binding.buttonLogin.setText(isLoading ? "Đang đăng nhập..." : "Đăng nhập");
                
                // Disable input fields during loading
                editTextEmail.setEnabled(!isLoading);
                editTextPassword.setEnabled(!isLoading);
                textSignUp.setEnabled(!isLoading);
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
