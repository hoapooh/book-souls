package com.example.book_souls_project.api.types.category;

import com.google.gson.annotations.SerializedName;

public class CategoryDetailResponse {
    @SerializedName("message")
    private String message;
    
    @SerializedName("result")
    private Category result;

    // Constructors
    public CategoryDetailResponse() {}

    public CategoryDetailResponse(String message, Category result) {
        this.message = message;
        this.result = result;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Category getResult() { return result; }
    public void setResult(Category result) { this.result = result; }

    @Override
    public String toString() {
        return "CategoryDetailResponse{" +
                "message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}
