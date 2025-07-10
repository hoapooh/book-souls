package com.example.book_souls_project.api.client;

import android.content.Context;
import android.util.Log;

import com.example.book_souls_project.api.ApiClient;
import com.example.book_souls_project.api.service.ChatService;
import com.example.book_souls_project.api.types.chat.ConversationResponse;
import com.example.book_souls_project.api.types.chat.MessageResponse;
import com.example.book_souls_project.api.types.chat.Conversation;
import com.example.book_souls_project.api.types.chat.ChatMessage;
import com.example.book_souls_project.util.TokenManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatApiClient {
    private static final String TAG = "ChatApiClient";
    
    private ChatService chatService;
    private TokenManager tokenManager;
    
    public ChatApiClient(Context context) {
        ApiClient apiClient = ApiClient.getInstance(context);
        this.chatService = apiClient.getRetrofit().create(ChatService.class);
        this.tokenManager = apiClient.getTokenManager();
    }
    
    public interface ConversationCallback {
        void onSuccess(List<Conversation> conversations);
        void onError(String errorMessage);
    }
    
    public interface MessageCallback {
        void onSuccess(List<ChatMessage> messages);
        void onError(String errorMessage);
    }
    
    public void getConversations(ConversationCallback callback) {
        String token = tokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            callback.onError("No access token available");
            return;
        }
        
        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;
        
        Log.d(TAG, "Fetching conversations with token: " + authHeader.substring(0, Math.min(authHeader.length(), 20)) + "...");
        
        Call<ConversationResponse> call = chatService.getConversations(authHeader);
        call.enqueue(new Callback<ConversationResponse>() {
            @Override
            public void onResponse(Call<ConversationResponse> call, Response<ConversationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ConversationResponse conversationResponse = response.body();
                    if (conversationResponse.getResult() != null && conversationResponse.getResult().getResult() != null) {
                        List<Conversation> conversations = conversationResponse.getResult().getResult();
                        Log.d(TAG, "Successfully fetched " + conversations.size() + " conversations");
                        callback.onSuccess(conversations);
                    } else {
                        Log.w(TAG, "No conversations found in response");
                        callback.onError("No conversations found");
                    }
                } else {
                    String errorMessage = "Failed to fetch conversations: " + response.code();
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }
            
            @Override
            public void onFailure(Call<ConversationResponse> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }
    
    public void getMessages(String conversationId, MessageCallback callback) {
        String token = tokenManager.getAccessToken();
        if (token == null || token.isEmpty()) {
            callback.onError("No access token available");
            return;
        }
        
        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;
        
        Log.d(TAG, "Fetching messages for conversation: " + conversationId);
        
        Call<MessageResponse> call = chatService.getMessages(authHeader, conversationId);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MessageResponse messageResponse = response.body();
                    if (messageResponse.getResult() != null && messageResponse.getResult().getResult() != null) {
                        List<ChatMessage> messages = messageResponse.getResult().getResult();
                        Log.d(TAG, "Successfully fetched " + messages.size() + " messages");
                        callback.onSuccess(messages);
                    } else {
                        Log.w(TAG, "No messages found in response");
                        callback.onError("No messages found");
                    }
                } else {
                    String errorMessage = "Failed to fetch messages: " + response.code();
                    Log.e(TAG, errorMessage);
                    callback.onError(errorMessage);
                }
            }
            
            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                String errorMessage = "Network error: " + t.getMessage();
                Log.e(TAG, errorMessage, t);
                callback.onError(errorMessage);
            }
        });
    }
}
