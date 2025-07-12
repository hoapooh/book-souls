package com.example.book_souls_project.api.types.order;

import com.google.gson.annotations.SerializedName;

public class OrderDetailResponse {
    @SerializedName("message")
    private String message;
    
    @SerializedName("result")
    private OrderDetail result;
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public OrderDetail getResult() {
        return result;
    }
    
    public void setResult(OrderDetail result) {
        this.result = result;
    }
}
