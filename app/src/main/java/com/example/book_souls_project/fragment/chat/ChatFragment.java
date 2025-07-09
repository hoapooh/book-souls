package com.example.book_souls_project.fragment.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private CardView btnSend;
    private ImageView btnBack;
    
    private ChatAdapter chatAdapter;
    private List<Message> messages;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadInitialMessages();
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

    private void loadInitialMessages() {
        // Add some initial messages to simulate a conversation
        addReceivedMessage("Hello! Welcome to BookSouls Support. How can I help you today?");
        addReceivedMessage("I'm here to assist you with any questions about books, orders, or technical issues.");
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        
        if (!TextUtils.isEmpty(messageText)) {
            // Add user message
            Message userMessage = new Message(messageText, true);
            chatAdapter.addMessage(userMessage);
            
            // Clear input
            editTextMessage.setText("");
            
            // Scroll to bottom immediately
            scrollToBottom();
            
            // Simulate support response after a short delay
            simulateSupportResponse(messageText);
        }
    }

    private void addReceivedMessage(String text) {
        Message receivedMessage = new Message(text, false);
        chatAdapter.addMessage(receivedMessage);
        // Delay scroll to ensure the item is added to the RecyclerView
        recyclerViewMessages.postDelayed(this::scrollToBottom, 100);
    }

    private void simulateSupportResponse(String userMessage) {
        // Simulate typing delay
        recyclerViewMessages.postDelayed(() -> {
            String response = generateSupportResponse(userMessage);
            addReceivedMessage(response);
        }, 1500); // 1.5 second delay
    }

    private String generateSupportResponse(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        if (lowerMessage.contains("order") || lowerMessage.contains("purchase")) {
            return "I can help you with your order! Could you please provide your order number or the email address used for the purchase?";
        } else if (lowerMessage.contains("book") || lowerMessage.contains("search")) {
            return "Looking for a specific book? I can help you find it! What genre or author are you interested in?";
        } else if (lowerMessage.contains("payment") || lowerMessage.contains("refund")) {
            return "For payment and refund issues, I'll be happy to assist. Please provide more details about the issue you're experiencing.";
        } else if (lowerMessage.contains("account") || lowerMessage.contains("login")) {
            return "Having trouble with your account? I can help you with login issues, password resets, or account settings.";
        } else if (lowerMessage.contains("delivery") || lowerMessage.contains("shipping")) {
            return "I can track your delivery status. Please share your order number and I'll check the shipping details for you.";
        } else if (lowerMessage.contains("thank") || lowerMessage.contains("thanks")) {
            return "You're welcome! Is there anything else I can help you with today?";
        } else if (lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
            return "Hello! I'm here to help. What can I assist you with today?";
        } else {
            return "I understand your concern. Could you please provide more details so I can assist you better? You can also call our support line at 1-800-BOOKSOULS for immediate assistance.";
        }
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
