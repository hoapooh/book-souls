package com.example.book_souls_project.api.types.chat;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ConversationResponse {
    @SerializedName("message")
    private String message;
    
    @SerializedName("result")
    private ConversationResult result;
    
    public ConversationResponse() {}
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public ConversationResult getResult() {
        return result;
    }
    
    public void setResult(ConversationResult result) {
        this.result = result;
    }
    
    public static class ConversationResult {
        @SerializedName("result")
        private List<Conversation> result;
        
        public ConversationResult() {}
        
        public List<Conversation> getResult() {
            return result;
        }
        
        public void setResult(List<Conversation> result) {
            this.result = result;
        }
    }
}
