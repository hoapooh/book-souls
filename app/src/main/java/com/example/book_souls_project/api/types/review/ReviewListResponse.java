package com.example.book_souls_project.api.types.review;

import java.util.List;

public class ReviewListResponse {
    private String message;
    private Result result;

    // Nested Result class to match API response
    public static class Result {
        private List<Review> items;
        private int totalCount;

        // Result getters and setters
        public List<Review> getItems() { return items; }
        public void setItems(List<Review> items) { this.items = items; }

        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    }

    // Constructors
    public ReviewListResponse() {}

    public ReviewListResponse(String message, Result result) {
        this.message = message;
        this.result = result;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Result getResult() { return result; }
    public void setResult(Result result) { this.result = result; }

    // Convenience methods to match existing code usage
    public List<Review> getData() { 
        return result != null ? result.getItems() : null; 
    }

    public int getTotalCount() { 
        return result != null ? result.getTotalCount() : 0; 
    }

    // For compatibility with pagination (if needed later)
    public int getCurrentPage() { return 1; }
    public int getTotalPages() { return 1; }
}
