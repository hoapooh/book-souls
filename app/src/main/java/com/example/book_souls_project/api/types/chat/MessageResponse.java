package com.example.book_souls_project.api.types.chat;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MessageResponse {
    @SerializedName("message")
    private String message;
    
    @SerializedName("result")
    private MessageResult result;
    
    public MessageResponse() {}
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public MessageResult getResult() {
        return result;
    }
    
    public void setResult(MessageResult result) {
        this.result = result;
    }
    
    public static class MessageResult {
        @SerializedName("result")
        private List<ChatMessage> result;
        
        public MessageResult() {}
        
        public List<ChatMessage> getResult() {
            return result;
        }
        
        public void setResult(List<ChatMessage> result) {
            this.result = result;
        }
    }
}
