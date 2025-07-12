package com.example.book_souls_project.api.types.order;

import java.util.List;

public class CreateOrderRequest {
    private String customerId;
    private List<OrderBook> orderBooks;

    public CreateOrderRequest() {}

    public CreateOrderRequest(String customerId, List<OrderBook> orderBooks) {
        this.customerId = customerId;
        this.orderBooks = orderBooks;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<OrderBook> getOrderBooks() {
        return orderBooks;
    }

    public void setOrderBooks(List<OrderBook> orderBooks) {
        this.orderBooks = orderBooks;
    }

    public static class OrderBook {
        private String bookId;
        private int quantity;

        public OrderBook() {}

        public OrderBook(String bookId, int quantity) {
            this.bookId = bookId;
            this.quantity = quantity;
        }

        public String getBookId() {
            return bookId;
        }

        public void setBookId(String bookId) {
            this.bookId = bookId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
