package com.example.book_souls_project.api.types.review;

public class ReviewCreateResponse {
    private String message;
    private Review result;

    // Constructors
    public ReviewCreateResponse() {}

    public ReviewCreateResponse(String message, Review result) {
        this.message = message;
        this.result = result;
    }

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Review getResult() { return result; }
    public void setResult(Review result) { this.result = result; }
}
