package com.example.book_souls_project.api.types.order;

import java.util.List;

public class Order {
    private String id;
    private String customerId;
    private String code;
    private double totalPrice;
    private String orderStatus;
    private String cancelReason;
    private String paymentStatus;
    private String createdAt;
    private List<OrderBook> orderBooks;

    public Order() {}

    public Order(String id, String customerId, String code, double totalPrice, 
                 String orderStatus, String cancelReason, String paymentStatus, 
                 String createdAt, List<OrderBook> orderBooks) {
        this.id = id;
        this.customerId = customerId;
        this.code = code;
        this.totalPrice = totalPrice;
        this.orderStatus = orderStatus;
        this.cancelReason = cancelReason;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
        this.orderBooks = orderBooks;
    }

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

    public List<OrderBook> getOrderBooks() {
        return orderBooks;
    }

    public void setOrderBooks(List<OrderBook> orderBooks) {
        this.orderBooks = orderBooks;
    }

    public static class OrderBook {
        private String bookId;
        private String bookTitle;
        private double bookPrice;
        private int quantity;

        public OrderBook() {}

        public OrderBook(String bookId, String bookTitle, double bookPrice, int quantity) {
            this.bookId = bookId;
            this.bookTitle = bookTitle;
            this.bookPrice = bookPrice;
            this.quantity = quantity;
        }

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
}
