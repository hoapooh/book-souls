package com.example.book_souls_project.api.types.notification;

public class CreateNotificationRequest {
    private String title;
    private String content;
    
    public CreateNotificationRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
}
