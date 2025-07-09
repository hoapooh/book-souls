package com.example.book_souls_project.fragment.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.book_souls_project.R;
import com.example.book_souls_project.adapter.ChatAdapter;
import com.example.book_souls_project.model.Message;
import com.example.book_souls_project.service.SignalRService;
import com.example.book_souls_project.util.TokenManager;
import com.example.book_souls_project.api.client.ChatApiClient;
import com.example.book_souls_project.api.types.chat.Conversation;
import com.example.book_souls_project.api.types.chat.ChatMessage;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class ChatFragment extends Fragment implements SignalRService.SignalRMessageListener {

    private static final String TAG = "ChatFragment";
    
    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private CardView btnSend;
    private ImageView btnBack;
    
    private ChatAdapter chatAdapter;
    private List<Message> messages;
    
    // SignalR and user management
    private SignalRService signalRService;
    private TokenManager tokenManager;
    private String currentUserId;
    private String supportUserId = "6859967a692e46a7b67c0123"; // Fixed support user ID
    
    // Chat API and conversation management
    private ChatApiClient chatApiClient;
    private String currentConversationId;
    
    // RxJava disposables for proper memory management
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize user management
        tokenManager = new TokenManager(requireContext());
        currentUserId = tokenManager.getUserId();
        
        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        
        // Initialize API client
        chatApiClient = new ChatApiClient(requireContext());
        
        // Load conversation and messages first, then initialize SignalR
        loadConversationAndMessages();
    }

    private void initViews(View view) {
        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true); // Start from bottom
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(chatAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        btnSend.setOnClickListener(v -> {
            sendMessage();
        });

        // Send message on Enter key
        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
        
        // Handle keyboard focus
        editTextMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Scroll to bottom when keyboard appears
                recyclerViewMessages.postDelayed(this::scrollToBottom, 300);
            }
        });
    }

    private void loadConversationAndMessages() {
        Log.d(TAG, "Loading conversation and messages...");
        
        // Show loading indicator
        // You can add a progress bar here if needed
        
        // First, get the conversation list
        chatApiClient.getConversations(new ChatApiClient.ConversationCallback() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                requireActivity().runOnUiThread(() -> {
                    if (conversations != null && !conversations.isEmpty()) {
                        // Get the first conversation (since user only chats with support)
                        Conversation firstConversation = conversations.get(0);
                        currentConversationId = firstConversation.getConversationId();
                        
                        Log.d(TAG, "Found conversation ID: " + currentConversationId);
                        
                        // Load messages for this conversation
                        loadMessagesForConversation(currentConversationId);
                    } else {
                        Log.d(TAG, "No existing conversations found, will create new one when sending first message");
                        // No existing conversation, show welcome message and initialize SignalR
                        loadWelcomeMessage();
                        initializeSignalR();
                    }
                });
            }
            
            @Override
            public void onError(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    Log.e(TAG, "Error loading conversations: " + errorMessage);
                    // Show welcome message and initialize SignalR anyway
                    loadWelcomeMessage();
                    initializeSignalR();
                });
            }
        });
    }
    
    private void loadMessagesForConversation(String conversationId) {
        Log.d(TAG, "Loading messages for conversation: " + conversationId);
        
        chatApiClient.getMessages(conversationId, new ChatApiClient.MessageCallback() {
            @Override
            public void onSuccess(List<ChatMessage> chatMessages) {
                requireActivity().runOnUiThread(() -> {
                    // Clear any existing messages
                    messages.clear();
                    
                    if (chatMessages != null && !chatMessages.isEmpty()) {
                        // Convert API messages to UI messages
                        for (ChatMessage chatMessage : chatMessages) {
                            boolean isSent = chatMessage.getSenderId().equals(currentUserId);
                            long timestamp = parseTimestamp(chatMessage.getSentAt());
                            
                            Message message = new Message(chatMessage.getText(), isSent, timestamp);
                            messages.add(message);
                        }
                        
                        Log.d(TAG, "Loaded " + messages.size() + " messages from API");
                        chatAdapter.notifyDataSetChanged();
                        scrollToBottom();
                    } else {
                        Log.d(TAG, "No messages found in conversation");
                    }
                    
                    // Add welcome message if no messages exist
                    if (messages.isEmpty()) {
                        loadWelcomeMessage();
                    }
                    
                    // Initialize SignalR after loading messages
                    initializeSignalR();
                });
            }
            
            @Override
            public void onError(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    Log.e(TAG, "Error loading messages: " + errorMessage);
                    Toast.makeText(requireContext(), "Error loading messages: " + errorMessage, Toast.LENGTH_SHORT).show();
                    
                    // Show welcome message and initialize SignalR anyway
                    loadWelcomeMessage();
                    initializeSignalR();
                });
            }
        });
    }
    
    private void loadWelcomeMessage() {
        // Add some initial messages to simulate a conversation
        addLocalMessage("Hello! Welcome to BookSouls Support. How can I help you today?", false);
        addLocalMessage("I'm here to assist you with any questions about books, orders, or technical issues.", false);
    }
    
    private long parseTimestamp(String timestamp) {
        try {
            // Parse ISO 8601 timestamp: "2025-07-09T18:46:48.838Z"
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = isoFormat.parse(timestamp);
            return date != null ? date.getTime() : System.currentTimeMillis();
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing timestamp: " + timestamp, e);
            return System.currentTimeMillis();
        }
    }
    
    private void initializeSignalR() {
        // Get access token from TokenManager
        String accessToken = tokenManager.getAccessToken();
        
        Log.d(TAG, "Initializing SignalR with user ID: " + currentUserId);
        Log.d(TAG, "Token available: " + (accessToken != null && !accessToken.isEmpty()));
        
        if (accessToken != null && !accessToken.isEmpty()) {
            Log.d(TAG, "Token length: " + accessToken.length());
            Log.d(TAG, "Token format check - starts with 'Bearer': " + accessToken.startsWith("Bearer"));
            // Create SignalR service with access token
            signalRService = new SignalRService(accessToken);
            Log.d(TAG, "SignalR service created with access token");
        } else {
            // Create SignalR service without token (fallback)
            signalRService = new SignalRService();
            Log.w(TAG, "SignalR service created without access token - this might cause authentication issues");
        }
        
        signalRService.setMessageListener(this);
        
        // Connect to SignalR hub
        Disposable connectionDisposable = signalRService.connect()
                .subscribe(
                    () -> {
                        requireActivity().runOnUiThread(() -> {
                            Log.d(TAG, "Successfully connected to SignalR");
                            Toast.makeText(requireContext(), "Connected to chat", Toast.LENGTH_SHORT).show();
                        });
                    },
                    exception -> {
                        requireActivity().runOnUiThread(() -> {
                            Log.e(TAG, "Failed to connect to SignalR", exception);
                            String errorDetails = exception.getMessage() != null ? exception.getMessage() : "Unknown error";
                            Toast.makeText(requireContext(), "Failed to connect: " + errorDetails, Toast.LENGTH_LONG).show();
                        });
                    }
                );
        
        // Add to composite disposable for proper cleanup
        compositeDisposable.add(connectionDisposable);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Dispose of all RxJava subscriptions to prevent memory leaks
        if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        
        if (signalRService != null) {
            signalRService.cleanup();
        }
    }
    
    // SignalR Event Handlers
    @Override
    public void onMessageSent(String message) {
        requireActivity().runOnUiThread(() -> {
            Log.d(TAG, "Message sent confirmation: " + message);
            // Message already added locally, just log confirmation
        });
    }
    
    @Override
    public void onMessageReceived(String message) {
        requireActivity().runOnUiThread(() -> {
            Log.d(TAG, "Message received: " + message);
            addLocalMessage(message, false); // Display on left side (from support)
        });
    }
    
    @Override
    public void onConnectionStateChanged(boolean isConnected) {
        requireActivity().runOnUiThread(() -> {
            if (isConnected) {
                Log.d(TAG, "Connected to SignalR");
                Toast.makeText(requireContext(), "Chat connected", Toast.LENGTH_SHORT).show();
                // Update UI to show connected state
            } else {
                Log.w(TAG, "Disconnected from SignalR");
                Toast.makeText(requireContext(), "Chat disconnected", Toast.LENGTH_SHORT).show();
                // Update UI to show disconnected state
                
                // Optional: Try to reconnect after a delay if we were previously connected
                if (signalRService != null) {
                    Log.d(TAG, "Attempting to reconnect in 3 seconds...");
                    recyclerViewMessages.postDelayed(() -> {
                        if (signalRService != null && !signalRService.isConnected()) {
                            Log.d(TAG, "Attempting reconnection...");
                            Disposable reconnectDisposable = signalRService.connect().subscribe(
                                () -> Log.d(TAG, "Reconnection successful"),
                                error -> Log.e(TAG, "Reconnection failed", error)
                            );
                            compositeDisposable.add(reconnectDisposable);
                        }
                    }, 3000);
                }
            }
        });
    }
    
    @Override
    public void onError(String error) {
        requireActivity().runOnUiThread(() -> {
            Log.e(TAG, "SignalR error: " + error);
            Toast.makeText(requireContext(), "Chat error: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        
        if (!TextUtils.isEmpty(messageText)) {
            if (signalRService == null || !signalRService.isConnected()) {
                Toast.makeText(requireContext(), "Not connected to chat server", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Add message to UI immediately (optimistic UI)
            addLocalMessage(messageText, true); // Display on right side (from user)
            
            // Clear input
            editTextMessage.setText("");
            
            // Scroll to bottom immediately
            scrollToBottom();
            
            // Send message via SignalR with fallback to alternative method
            Log.d(TAG, "Attempting to send message via SignalR");
            Log.d(TAG, "Current user ID: " + currentUserId);
            Log.d(TAG, "Support user ID: " + supportUserId);
            Log.d(TAG, "Conversation ID: " + currentConversationId);
            Log.d(TAG, "Message text: " + messageText);
            
            Disposable sendDisposable = signalRService.sendMessage(
                currentConversationId, // Use the actual conversation ID
                currentUserId, // senderId
                supportUserId, // receiverId
                messageText // text
            ).subscribe(
                () -> {
                    requireActivity().runOnUiThread(() -> {
                        Log.d(TAG, "Message sent successfully via SignalR");
                    });
                },
                exception -> {
                    requireActivity().runOnUiThread(() -> {
                        Log.e(TAG, "First send method failed, trying alternative", exception);
                        
                        // Try alternative method without conversationId
                        Disposable altDisposable = signalRService.sendMessageAlternative(currentUserId, supportUserId, messageText)
                                .subscribe(
                                    () -> {
                                        Log.d(TAG, "Message sent successfully via alternative method");
                                        Toast.makeText(requireContext(), "Message sent (alt method)", Toast.LENGTH_SHORT).show();
                                    },
                                    altException -> {
                                        Log.e(TAG, "Both send methods failed", altException);
                                        Toast.makeText(requireContext(), 
                                            "Send failed: " + altException.getMessage(), 
                                            Toast.LENGTH_LONG).show();
                                        // Mark message as failed or remove it
                                    }
                                );
                        
                        // Add alternative disposable to composite
                        compositeDisposable.add(altDisposable);
                    });
                }
            );
            
            // Add to composite disposable for proper cleanup
            compositeDisposable.add(sendDisposable);
        }
    }
    
    private void addLocalMessage(String text, boolean isSent) {
        Message message = new Message(text, isSent);
        chatAdapter.addMessage(message);
        // Delay scroll to ensure the item is added to the RecyclerView
        recyclerViewMessages.postDelayed(this::scrollToBottom, 100);
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            recyclerViewMessages.post(() -> {
                try {
                    recyclerViewMessages.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                } catch (Exception e) {
                    // Fallback to regular scroll if smooth scroll fails
                    recyclerViewMessages.scrollToPosition(chatAdapter.getItemCount() - 1);
                }
            });
        }
    }
}
