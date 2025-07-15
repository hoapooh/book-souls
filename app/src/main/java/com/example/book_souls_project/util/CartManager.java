package com.example.book_souls_project.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.book_souls_project.api.types.book.Book;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String TAG = "CartManager";
    private static final String PREFS_NAME = "book_souls_cart";
    private static final String KEY_CART_ITEMS = "cart_items";
    private static final String KEY_CART_QUANTITIES = "cart_quantities";
    private static CartManager instance;
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    public CartManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }
    // Cart item model
    public static class CartItem {
        private Book book;
        private int quantity;
        
        public CartItem(Book book, int quantity) {
            this.book = book;
            this.quantity = quantity;
        }
        
        public Book getBook() { return book; }
        public void setBook(Book book) { this.book = book; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
        public int getTotalPrice() {
            return book.getPrice() * quantity;
        }
    }
    
    // Add book to cart
    public boolean addToCart(Book book, int quantity) {
        try {
            List<CartItem> cartItems = getCartItems();
            
            // Check if book already exists in cart
            boolean found = false;
            for (CartItem item : cartItems) {
                if (item.getBook().getId().equals(book.getId())) {
                    item.setQuantity(item.getQuantity() + quantity);
                    found = true;
                    break;
                }
            }
            
            // If not found, add new item
            if (!found) {
                cartItems.add(new CartItem(book, quantity));
            }
            
            // Save to SharedPreferences
            saveCartItems(cartItems);
            
            Log.d(TAG, "Added " + quantity + " of " + book.getTitle() + " to cart");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error adding book to cart", e);
            return false;
        }
    }
    
    // Remove book from cart
    public boolean removeFromCart(String bookId) {
        try {
            List<CartItem> cartItems = getCartItems();
            cartItems.removeIf(item -> item.getBook().getId().equals(bookId));
            saveCartItems(cartItems);
            
            Log.d(TAG, "Removed book " + bookId + " from cart");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error removing book from cart", e);
            return false;
        }
    }
    
    // Update quantity of book in cart
    public boolean updateQuantity(String bookId, int newQuantity) {
        try {
            List<CartItem> cartItems = getCartItems();
            
            for (CartItem item : cartItems) {
                if (item.getBook().getId().equals(bookId)) {
                    if (newQuantity <= 0) {
                        return removeFromCart(bookId);
                    } else {
                        item.setQuantity(newQuantity);
                        saveCartItems(cartItems);
                        return true;
                    }
                }
            }
            
            Log.w(TAG, "Book " + bookId + " not found in cart");
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating quantity", e);
            return false;
        }
    }
    
    // Get all cart items
    public List<CartItem> getCartItems() {
        try {
            String cartJson = sharedPreferences.getString(KEY_CART_ITEMS, "[]");
            Type type = new TypeToken<List<CartItem>>(){}.getType();
            List<CartItem> items = gson.fromJson(cartJson, type);
            return items != null ? items : new ArrayList<>();
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting cart items", e);
            return new ArrayList<>();
        }
    }
    
    // Get cart item count
    public int getCartItemCount() {
        List<CartItem> items = getCartItems();
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }
    
    // Get total cart price
    public int getTotalCartPrice() {
        List<CartItem> items = getCartItems();
        int total = 0;
        for (CartItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }
    
    // Check if book is in cart
    public boolean isInCart(String bookId) {
        List<CartItem> items = getCartItems();
        for (CartItem item : items) {
            if (item.getBook().getId().equals(bookId)) {
                return true;
            }
        }
        return false;
    }
    
    // Get quantity of specific book in cart
    public int getBookQuantityInCart(String bookId) {
        List<CartItem> items = getCartItems();
        for (CartItem item : items) {
            if (item.getBook().getId().equals(bookId)) {
                return item.getQuantity();
            }
        }
        return 0;
    }
    
    // Clear all cart items
    public void clearCart() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_CART_ITEMS);
        editor.apply();
        Log.d(TAG, "Cart cleared");
    }
    
    // Save cart items to SharedPreferences
    private void saveCartItems(List<CartItem> cartItems) {
        String cartJson = gson.toJson(cartItems);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CART_ITEMS, cartJson);
        editor.apply();
    }
}
