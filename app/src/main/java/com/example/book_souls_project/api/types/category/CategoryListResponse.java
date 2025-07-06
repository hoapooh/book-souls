package com.example.book_souls_project.api.types.category;

import java.util.List;

public class CategoryListResponse {
    private String message;
    private Result result;

    // Constructors
    public CategoryListResponse() {}

    public CategoryListResponse(String message, Result result) {
        this.message = message;
        this.result = result;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Result getResult() { return result; }
    public void setResult(Result result) { this.result = result; }

    public static class Result {
        private List<Category> items;
        private int totalCount;

        public List<Category> getItems() { return items; }
        public void setItems(List<Category> items) { this.items = items; }

        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    }

    @Override
    public String toString() {
        return "CategoryListResponse{" +
                "message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}
