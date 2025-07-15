package com.example.book_souls_project.api.types.order;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderDetail {
    @SerializedName("id")
    private String id;
    
    @SerializedName("customerId")
    private String customerId;
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("totalPrice")
    private double totalPrice;
    
    @SerializedName("orderStatus")
    private String orderStatus;
    
    @SerializedName("cancelReason")
    private String cancelReason;
    
    @SerializedName("paymentStatus")
    private String paymentStatus;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("orderBooks")
    private List<OrderBookDetail> orderBooks;
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public String getOrderStatus() {
        return orderStatus;
    }
    
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
    
    public String getCancelReason() {
        return cancelReason;
    }
    
    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
    
    public String getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<OrderBookDetail> getOrderBooks() {
        return orderBooks;
    }
    
    public void setOrderBooks(List<OrderBookDetail> orderBooks) {
        this.orderBooks = orderBooks;
    }
}
