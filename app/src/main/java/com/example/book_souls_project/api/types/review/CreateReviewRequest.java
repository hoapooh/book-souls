package com.example.book_souls_project.api.types.review;

public class CreateReviewRequest {
    private String bookId;
    private float rating;
    private String content;

    // Constructors
    public CreateReviewRequest() {}

    public CreateReviewRequest(String bookId, float rating, String content) {
        this.bookId = bookId;
        this.rating = rating;
        this.content = content;
    }

    // Getters and Setters
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
