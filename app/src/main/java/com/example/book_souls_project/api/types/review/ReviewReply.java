package com.example.book_souls_project.api.types.review;

import java.util.Date;

public class ReviewReply {
    private String id;
    private String reviewId;
    private String userId;
    private String userName;
    private String userAvatarUrl;
    private String content;
    private Date createdAt;

    // Constructors
    public ReviewReply() {}

    public ReviewReply(String id, String reviewId, String userId, String userName, 
                      String userAvatarUrl, String content, Date createdAt) {
        this.id = id;
        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatarUrl = userAvatarUrl;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserAvatarUrl() { return userAvatarUrl; }
    public void setUserAvatarUrl(String userAvatarUrl) { this.userAvatarUrl = userAvatarUrl; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // Utility method for formatted date display
    public String getFormattedDate() {
        if (createdAt == null) return "";
        
        Date now = new Date();
        long diffInMillis = now.getTime() - createdAt.getTime();
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
        
        if (diffInDays == 0) return "Today";
        if (diffInDays == 1) return "Yesterday";
        if (diffInDays < 7) return diffInDays + " days ago";
        if (diffInDays < 30) return (diffInDays / 7) + " weeks ago";
        if (diffInDays < 365) return (diffInDays / 30) + " months ago";
        return (diffInDays / 365) + " years ago";
    }
}
