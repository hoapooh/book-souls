package com.example.book_souls_project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.book_souls_project.R;
import com.example.book_souls_project.databinding.AuthenticationLoginBinding;

public class LoginFragment extends Fragment {
    private AuthenticationLoginBinding binding;
    private TextView textSignUp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        binding = AuthenticationLoginBinding.inflate(inflater, container, false);
        textSignUp = binding.textSignUp;

//        View view = inflater.inflate(R.layout.authentication_login, container, false);
//        TextView textSignUp = view.findViewById(R.id.textSignUp);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigate to signup
        textSignUp.setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_LoginFragment_to_SignupFragment);
        });

        // Handle login button click
        binding.buttonLogin.setOnClickListener(v -> {
            // For demo purposes, navigate directly to home
            // In a real app, you would validate credentials first
            NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_LoginFragment_to_Home);
        });

        // Handle test button click for quick navigation to home
        binding.buttonTestHome.setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_LoginFragment_to_Home);
        });
    }
}
