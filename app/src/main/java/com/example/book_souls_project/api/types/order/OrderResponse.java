package com.example.book_souls_project.api.types.order;

import java.util.List;

public class OrderResponse {
    private String message;
    private OrderResult result;

    public OrderResponse() {}

    public OrderResponse(String message, OrderResult result) {
        this.message = message;
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OrderResult getResult() {
        return result;
    }

    public void setResult(OrderResult result) {
        this.result = result;
    }

    public static class OrderResult {
        private List<Order> items;
        private int totalCount;

        public OrderResult() {}

        public OrderResult(List<Order> items, int totalCount) {
            this.items = items;
            this.totalCount = totalCount;
        }

        public List<Order> getItems() {
            return items;
        }

        public void setItems(List<Order> items) {
            this.items = items;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }
    }
}
