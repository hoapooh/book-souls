package com.example.book_souls_project.api.service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PaymentService {
    
    @POST("payment/{orderId}/checkout-url")
    Call<ResponseBody> retryPayment(@Path("orderId") String orderId);
}
