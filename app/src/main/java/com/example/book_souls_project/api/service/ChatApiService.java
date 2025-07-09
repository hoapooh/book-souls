package com.example.book_souls_project.api.service;

import com.example.book_souls_project.api.types.chat.ConversationResponse;
import com.example.book_souls_project.api.types.chat.MessageResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface ChatApiService {
    
    @GET("/api/chat/conversations")
    Call<ConversationResponse> getConversations(@Header("Authorization") String authToken);
    
    @GET("/api/chat/{conversationId}/messages")
    Call<MessageResponse> getMessages(
        @Header("Authorization") String authToken,
        @Path("conversationId") String conversationId
    );
}
