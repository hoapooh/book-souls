package com.example.book_souls_project.api.types.order;

import com.google.gson.annotations.SerializedName;

public class OrderBookDetail {
    @SerializedName("bookId")
    private String bookId;
    
    @SerializedName("bookTitle")
    private String bookTitle;
    
    @SerializedName("bookPrice")
    private double bookPrice;
    
    @SerializedName("quantity")
    private int quantity;
    
    // Getters and setters
    public String getBookId() {
        return bookId;
    }
    
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public double getBookPrice() {
        return bookPrice;
    }
    
    public void setBookPrice(double bookPrice) {
        this.bookPrice = bookPrice;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
