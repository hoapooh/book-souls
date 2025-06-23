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

        // Use inside fragment, preferable way
        textSignUp.setOnClickListener(v -> {
            NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_LoginFragment_to_SignupFragment);
        });

        // Use inside activity or custom Views
        /*textLogIn.setOnClickListener(v -> {
            NavHostFragment.findNavController(SignUpFragment.this).navigate(R.id.action_SignupFragment_to_LoginFragment);
        });*/
    }
}
