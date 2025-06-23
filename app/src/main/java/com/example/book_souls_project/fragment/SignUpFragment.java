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

public class SignUpFragment extends Fragment {
    private TextView textLogIn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.authentication_signup, container, false);
        textLogIn = view.findViewById(R.id.textLogIn);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Use inside fragment, preferable way
        textLogIn.setOnClickListener(v -> {
            NavHostFragment.findNavController(SignUpFragment.this).navigate(R.id.action_SignupFragment_to_LoginFragment);
        });

        // Use inside activity or custom Views
        /*textLogIn.setOnClickListener(v -> {
            NavHostFragment.findNavController(SignUpFragment.this).navigate(R.id.action_SignupFragment_to_LoginFragment);
        });*/
    }
}
