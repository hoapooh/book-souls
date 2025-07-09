package com.example.book_souls_project.api.types.chat;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    @SerializedName("id")
    private String id;
    
    @SerializedName("conversationId")
    private String conversationId;
    
    @SerializedName("senderId")
    private String senderId;
    
    @SerializedName("receiverId")
    private String receiverId;
    
    @SerializedName("text")
    private String text;
    
    @SerializedName("sentAt")
    private String sentAt;
    
    @SerializedName("isRead")
    private boolean isRead;
    
    @SerializedName("isDeleted")
    private boolean isDeleted;
    
    // Constructors
    public ChatMessage() {}
    
    public ChatMessage(String id, String conversationId, String senderId, String receiverId, 
                      String text, String sentAt, boolean isRead, boolean isDeleted) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.sentAt = sentAt;
        this.isRead = isRead;
        this.isDeleted = isDeleted;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    
    public String getReceiverId() {
        return receiverId;
    }
    
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public boolean isDeleted() {
        return isDeleted;
    }
    
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
