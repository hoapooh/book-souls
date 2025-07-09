package com.example.book_souls_project.api.types.chat;

import com.google.gson.annotations.SerializedName;

public class Conversation {
    @SerializedName("conversationId")
    private String conversationId;
    
    @SerializedName("otherUserId")
    private String otherUserId;
    
    @SerializedName("lastMessage")
    private String lastMessage;
    
    @SerializedName("lastSenderId")
    private String lastSenderId;
    
    @SerializedName("lastSentAt")
    private String lastSentAt;
    
    // Constructors
    public Conversation() {}
    
    public Conversation(String conversationId, String otherUserId, String lastMessage, 
                       String lastSenderId, String lastSentAt) {
        this.conversationId = conversationId;
        this.otherUserId = otherUserId;
        this.lastMessage = lastMessage;
        this.lastSenderId = lastSenderId;
        this.lastSentAt = lastSentAt;
    }
    
    // Getters and Setters
    public String getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
    
    public String getOtherUserId() {
        return otherUserId;
    }
    
    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }
    
    public String getLastMessage() {
        return lastMessage;
    }
    
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
    
    public String getLastSenderId() {
        return lastSenderId;
    }
    
    public void setLastSenderId(String lastSenderId) {
        this.lastSenderId = lastSenderId;
    }
    
    public String getLastSentAt() {
        return lastSentAt;
    }
    
    public void setLastSentAt(String lastSentAt) {
        this.lastSentAt = lastSentAt;
    }
}
