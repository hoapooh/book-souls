package com.example.book_souls_project.api.types.review;

public class ReviewCreateRequest {
    private String bookId;
    private String userId;
    private double rating; // Changed from int to double to support half stars
    private String comment;

    // Constructors
    public ReviewCreateRequest() {}

    public ReviewCreateRequest(String bookId, String userId, double rating, String comment) {
        this.bookId = bookId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
