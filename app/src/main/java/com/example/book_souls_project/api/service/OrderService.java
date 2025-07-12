package com.example.book_souls_project.api.service;

import com.example.book_souls_project.api.types.order.CreateOrderRequest;
import com.example.book_souls_project.api.types.order.OrderResponse;
import com.example.book_souls_project.api.types.order.OrderDetailResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OrderService {
    String ORDERS = "orders";

    // Create order - returns payment URL as plain text
    @POST(ORDERS)
    Call<ResponseBody> createOrder(@Body CreateOrderRequest request);
    
    // Get orders with filters
    @GET(ORDERS)
    Call<OrderResponse> getOrders(
        @Query("CustomerId") String customerId,
        @Query("OrderStatus") String orderStatus,
        @Query("PaymentStatus") String paymentStatus,
        @Query("pageIndex") int pageIndex,
        @Query("limit") int limit
    );

    // Get order by ID
    @GET(ORDERS + "/{id}")
    Call<OrderDetailResponse> getOrderById(@Path("id") String orderId);
}
