package com.example.book_souls_project.api.types.review;

public class CreateReviewResponse {
    private boolean success;
    private String message;
    private Review result;

    // Constructors
    public CreateReviewResponse() {}

    public CreateReviewResponse(boolean success, String message, Review result) {
        this.success = success;
        this.message = message;
        this.result = result;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Review getResult() { return result; }
    public void setResult(Review result) { this.result = result; }
}
