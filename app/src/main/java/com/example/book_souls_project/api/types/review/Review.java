package com.example.book_souls_project.api.types.review;

import java.util.Date;

public class Review {
    private String id;
    private User user;
    private String bookId;
    private double rating;
    private String comment;
    private String createdAt;
    private String updatedAt;

    // Nested User class to match API response
    public static class User {
        private String id;
        private String fullName;
        private String email;
        private String avatar;

        // User getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }

    // Constructors
    public Review() {}

    public Review(String id, User user, String bookId, double rating, String comment, String createdAt) {
        this.id = id;
        this.user = user;
        this.bookId = bookId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Utility method for formatted date display
    public String getFormattedDate() {
        if (createdAt == null || createdAt.isEmpty()) return "";
        
        try {
            // Parse ISO date string and calculate relative time
            // For now, return a simple format
            return createdAt.substring(0, 10); // Returns just the date part (YYYY-MM-DD)
        } catch (Exception e) {
            return "Recently";
        }
    }
}
