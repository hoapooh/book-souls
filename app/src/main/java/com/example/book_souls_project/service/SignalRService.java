package com.example.book_souls_project.service;

import android.util.Log;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.HttpHubConnectionBuilder;
import com.microsoft.signalr.TransportEnum;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class SignalRService {
    private static final String TAG = "SignalRService";
    private static final String HUB_URL = "https://booksoulsapp.onrender.com/chat";
    
    private HubConnection hubConnection;
    private SignalRMessageListener messageListener;
    private String accessToken;
    
    public interface SignalRMessageListener {
        void onMessageSent(String message);
        void onMessageReceived(String message);
        void onConnectionStateChanged(boolean isConnected);
        void onError(String error);
    }
    
    public SignalRService(String accessToken) {
        this.accessToken = accessToken;
        initializeConnection();
    }
    
    // Constructor without token for backward compatibility
    public SignalRService() {
        this.accessToken = null;
        initializeConnection();
    }
    
    private void initializeConnection() {
        try {
            Log.d(TAG, "Initializing SignalR connection to: " + HUB_URL);
            HttpHubConnectionBuilder builder = HubConnectionBuilder.create(HUB_URL);
            
            // Add access token if provided (equivalent to accessTokenFactory in React)
            if (accessToken != null && !accessToken.isEmpty()) {
                Log.d(TAG, "Configuring access token provider for SignalR");
                Log.d(TAG, "Token length: " + accessToken.length());
                Log.d(TAG, "Token starts with 'Bearer': " + accessToken.startsWith("Bearer"));
                
                builder = builder.withAccessTokenProvider(Single.fromCallable(() -> {
                    Log.d(TAG, "Access token provider called - providing token to SignalR");
                    // Some SignalR servers expect the token without "Bearer " prefix
                    // If your token already includes "Bearer ", remove it
                    String tokenToSend = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
                    Log.d(TAG, "Sending token (length: " + tokenToSend.length() + ")");
                    return tokenToSend;
                }));
                Log.d(TAG, "Access token provider configured for SignalR connection");
            } else {
                Log.w(TAG, "No access token provided - connecting without authentication");
            }
            
            // Configure transport to use WebSockets (like your React code)
            hubConnection = builder
                    .withTransport(TransportEnum.WEBSOCKETS) // Equivalent to HttpTransportType.WebSockets
                    .shouldSkipNegotiate(false) // Equivalent to skipNegotiation: false
                    .build();
                    
            setupEventHandlers();
            Log.d(TAG, "SignalR connection initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing SignalR connection", e);
            if (messageListener != null) {
                messageListener.onError("Failed to initialize connection: " + e.getMessage());
            }
        }
    }
    
    private void setupEventHandlers() {
        if (hubConnection == null) return;
        
        // Handle MessageSent event (messages we sent)
        hubConnection.on("MessageSent", (message) -> {
            Log.d(TAG, "MessageSent received: " + message);
            if (messageListener != null) {
                messageListener.onMessageSent(message);
            }
        }, String.class);
        
        // Handle ReceiveMessage event (messages from others)
        hubConnection.on("ReceiveMessage", (message) -> {
            Log.d(TAG, "ReceiveMessage received: " + message);
            if (messageListener != null) {
                messageListener.onMessageReceived(message);
            }
        }, String.class);
        
        // Handle ReceiveException event (server errors)
        hubConnection.on("ReceiveException", (errorMessage) -> {
            Log.e(TAG, "ReceiveException from server: " + errorMessage);
            if (messageListener != null) {
                messageListener.onError("Server error: " + errorMessage);
            }
        }, String.class);
        
        // Handle connection state changes
        hubConnection.onClosed((exception) -> {
            String errorMessage = "Connection closed";
            if (exception != null) {
                errorMessage += ": " + exception.getMessage();
                Log.e(TAG, "SignalR connection closed with error", exception);
            } else {
                Log.w(TAG, "SignalR connection closed normally");
            }
            
            if (messageListener != null) {
                messageListener.onConnectionStateChanged(false);
                if (exception != null) {
                    messageListener.onError("Connection lost: " + exception.getMessage());
                }
            }
        });
    }
    
    public void setMessageListener(SignalRMessageListener listener) {
        this.messageListener = listener;
    }
    
    public void updateAccessToken(String newAccessToken) {
        this.accessToken = newAccessToken;
        Log.d(TAG, "Access token updated. Reconnection required to apply changes.");
    }
    
    public Completable reconnectWithNewToken(String newAccessToken) {
        this.accessToken = newAccessToken;
        
        // Disconnect first, then reinitialize with new token
        return disconnect()
                .andThen(Completable.fromAction(() -> {
                    initializeConnection();
                }))
                .andThen(connect());
    }
    
    public Completable connect() {
        if (hubConnection == null) {
            initializeConnection();
        }
        
        if (hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED) {
            Log.d(TAG, "Connecting to SignalR hub at: " + HUB_URL);
            Log.d(TAG, "Access token provided: " + (accessToken != null && !accessToken.isEmpty()));
            
            return hubConnection.start()
                    .doOnComplete(() -> {
                        Log.i(TAG, "Connected to SignalR hub successfully");
                        Log.d(TAG, "Connection state: " + hubConnection.getConnectionState());
                        if (messageListener != null) {
                            messageListener.onConnectionStateChanged(true);
                        }
                    })
                    .doOnError(exception -> {
                        Log.e(TAG, "Error connecting to SignalR hub", exception);
                        Log.e(TAG, "Connection state after error: " + hubConnection.getConnectionState());
                        if (messageListener != null) {
                            messageListener.onError("Failed to connect: " + exception.getMessage());
                        }
                    });
        }
        
        Log.d(TAG, "Already connected or connecting. Current state: " + hubConnection.getConnectionState());
        return Completable.complete();
    }
    
    public Completable disconnect() {
        if (hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
            Log.d(TAG, "Disconnecting from SignalR hub...");
            return hubConnection.stop()
                    .doOnComplete(() -> {
                        Log.i(TAG, "Disconnected from SignalR hub");
                        if (messageListener != null) {
                            messageListener.onConnectionStateChanged(false);
                        }
                    });
        }
        
        return Completable.complete();
    }
    
    public Completable sendMessage(String conversationId, String senderId, String receiverId, String text) {
        if (hubConnection == null || hubConnection.getConnectionState() != HubConnectionState.CONNECTED) {
            Log.w(TAG, "Cannot send message: not connected to hub");
            if (messageListener != null) {
                messageListener.onError("Not connected to server");
            }
            return Completable.error(new Exception("Not connected"));
        }
        
        Log.d(TAG, "Preparing to send message:");
        Log.d(TAG, "  - conversationId: " + conversationId);
        Log.d(TAG, "  - senderId: " + senderId);
        Log.d(TAG, "  - receiverId: " + receiverId);
        Log.d(TAG, "  - text: " + text);
        Log.d(TAG, "  - text length: " + (text != null ? text.length() : "null"));
        
        // Try different parameter combinations to match server expectations
        // Some SignalR servers expect different parameter orders or types
        
        try {
            // Method 1: Send with null conversationId as originally intended
            Log.d(TAG, "Attempting to invoke SendMessage with null conversationId");
            return hubConnection.invoke("SendMessage", conversationId, senderId, receiverId, text)
                    .doOnComplete(() -> {
                        Log.d(TAG, "SendMessage completed successfully");
                    })
                    .doOnError(exception -> {
                        Log.e(TAG, "Error sending message (Method 1)", exception);
                        Log.e(TAG, "Exception type: " + exception.getClass().getSimpleName());
                        Log.e(TAG, "Exception message: " + exception.getMessage());
                        if (messageListener != null) {
                            messageListener.onError("Failed to send message: " + exception.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception during sendMessage setup", e);
            if (messageListener != null) {
                messageListener.onError("Failed to setup message sending: " + e.getMessage());
            }
            return Completable.error(e);
        }
    }
    
    // Alternative method to try different parameter combinations
    public Completable sendMessageAlternative(String senderId, String receiverId, String text) {
        if (hubConnection == null || hubConnection.getConnectionState() != HubConnectionState.CONNECTED) {
            Log.w(TAG, "Cannot send message: not connected to hub");
            return Completable.error(new Exception("Not connected"));
        }
        
        Log.d(TAG, "Attempting alternative SendMessage method without conversationId");
        
        // Try sending without conversationId parameter
        return hubConnection.invoke("SendMessage", senderId, receiverId, text)
                .doOnComplete(() -> {
                    Log.d(TAG, "Alternative SendMessage completed successfully");
                })
                .doOnError(exception -> {
                    Log.e(TAG, "Error in alternative sendMessage", exception);
                    if (messageListener != null) {
                        messageListener.onError("Alternative send failed: " + exception.getMessage());
                    }
                });
    }
    
    public boolean isConnected() {
        return hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED;
    }
    
    public HubConnectionState getConnectionState() {
        return hubConnection != null ? hubConnection.getConnectionState() : HubConnectionState.DISCONNECTED;
    }
    
    public void cleanup() {
        if (hubConnection != null) {
            disconnect().subscribe(
                () -> Log.d(TAG, "Cleanup completed"),
                error -> Log.e(TAG, "Error during cleanup", error)
            );
            hubConnection = null;
        }
        messageListener = null;
    }
}
