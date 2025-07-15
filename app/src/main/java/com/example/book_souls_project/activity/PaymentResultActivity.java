package com.example.book_souls_project.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.book_souls_project.MainActivity;
import com.example.book_souls_project.R;
import com.google.android.material.button.MaterialButton;

public class PaymentResultActivity extends AppCompatActivity {
    
    private LinearLayout layoutLoading, layoutSuccess, layoutFailed;
    private TextView textSuccessOrderCode, textFailedTitle, textFailedMessage;
    private MaterialButton buttonBackToHome, buttonViewOrders, buttonTryAgain, buttonBackToHomeFromFailed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);
        
        initViews();
        setupClickListeners();
        
        // Simulate processing delay
        new Handler().postDelayed(this::handlePaymentResult, 2000);
    }
    
    private void initViews() {
        layoutLoading = findViewById(R.id.layoutLoading);
        layoutSuccess = findViewById(R.id.layoutSuccess);
        layoutFailed = findViewById(R.id.layoutFailed);
        
        textSuccessOrderCode = findViewById(R.id.textSuccessOrderCode);
        textFailedTitle = findViewById(R.id.textFailedTitle);
        textFailedMessage = findViewById(R.id.textFailedMessage);
        
        buttonBackToHome = findViewById(R.id.buttonBackToHome);
        buttonViewOrders = findViewById(R.id.buttonViewOrders);
        buttonTryAgain = findViewById(R.id.buttonTryAgain);
        buttonBackToHomeFromFailed = findViewById(R.id.buttonBackToHomeFromFailed);
    }
    
    private void setupClickListeners() {
        buttonBackToHome.setOnClickListener(v -> navigateToHome(true));
        buttonViewOrders.setOnClickListener(v -> navigateToOrders());
        buttonTryAgain.setOnClickListener(v -> navigateToCart());
        buttonBackToHomeFromFailed.setOnClickListener(v -> navigateToHome(false));
    }

    private void handlePaymentResult() {
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            // Parse payment result from URL parameters
            String code = data.getQueryParameter("code");
            String id = data.getQueryParameter("id");
            String cancel = data.getQueryParameter("cancel");
            String status = data.getQueryParameter("status");
            String orderCode = data.getQueryParameter("orderCode");

            if ("00".equals(code) && "false".equals(cancel) && "PAID".equals(status)) {
                // Payment successful
                showPaymentSuccess(orderCode);
            } else if ("true".equals(cancel) || "CANCELLED".equals(status)) {
                // Payment cancelled
                showPaymentCancelled();
            } else {
                // Payment failed
                showPaymentFailed();
            }
        } else {
            // No data received
            showPaymentFailed();
        }
    }

    private void showPaymentSuccess(String orderCode) {
        layoutLoading.setVisibility(View.GONE);
        layoutFailed.setVisibility(View.GONE);
        layoutSuccess.setVisibility(View.VISIBLE);
        
        if (orderCode != null && !orderCode.isEmpty()) {
            textSuccessOrderCode.setText("#" + orderCode);
        }
    }

    private void showPaymentCancelled() {
        layoutLoading.setVisibility(View.GONE);
        layoutSuccess.setVisibility(View.GONE);
        layoutFailed.setVisibility(View.VISIBLE);
        
        textFailedTitle.setText("Payment Cancelled");
        textFailedMessage.setText("You cancelled the payment process. Your order has not been completed.");
        
        // Update button text for cancelled case
        buttonTryAgain.setText("Continue Shopping");
    }

    private void showPaymentFailed() {
        layoutLoading.setVisibility(View.GONE);
        layoutSuccess.setVisibility(View.GONE);
        layoutFailed.setVisibility(View.VISIBLE);
        
        textFailedTitle.setText("Payment Failed");
        textFailedMessage.setText("Your payment was not processed successfully. Please try again or contact support.");
        
        // Reset button text for failed case
        buttonTryAgain.setText("Try Again");
    }
    
    private void navigateToHome(boolean paymentSuccess) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("payment_success", paymentSuccess);
        intent.putExtra("navigate_to", "home");
        startActivity(intent);
        finish();
    }
    
    private void navigateToOrders() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("payment_success", true);
        intent.putExtra("navigate_to", "orders");
        startActivity(intent);
        finish();
    }
    
    private void navigateToCart() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("navigate_to", "cart");
        startActivity(intent);
        finish();
    }
}
