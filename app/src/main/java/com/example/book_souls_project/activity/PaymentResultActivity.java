package com.example.book_souls_project.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.book_souls_project.MainActivity;
import com.example.book_souls_project.R;

public class PaymentResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        handlePaymentResult();
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
            } else {
                // Payment failed or cancelled
                showPaymentFailed();
            }
        } else {
            // No data received
            showPaymentFailed();
        }
    }

    private void showPaymentSuccess(String orderCode) {
        Toast.makeText(this, "Payment successful! Order: " + orderCode, Toast.LENGTH_LONG).show();
        
        // Navigate to main activity and show success
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("payment_success", true);
        intent.putExtra("order_code", orderCode);
        startActivity(intent);
        finish();
    }

    private void showPaymentFailed() {
        Toast.makeText(this, "Payment failed or cancelled", Toast.LENGTH_LONG).show();
        
        // Navigate back to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("payment_success", false);
        startActivity(intent);
        finish();
    }
}
