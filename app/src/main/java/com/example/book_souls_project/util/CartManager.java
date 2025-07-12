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
    
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    public CartManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    // Cart item model với selection state
    public static class CartItem {
        private Book book;
        private int quantity;
        private boolean isSelected; // Thêm trạng thái được chọn
        
        public CartItem(Book book, int quantity) {
            this.book = book;
            this.quantity = quantity;
            this.isSelected = false; // Mặc định không được chọn
        }
        
        public Book getBook() { return book; }
        public void setBook(Book book) { this.book = book; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
        public boolean isSelected() { return isSelected; }
        public void setSelected(boolean selected) { isSelected = selected; }
        
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
    
    // Thêm các phương thức mới cho selection
    
    // Select/deselect item
    public boolean setItemSelected(String bookId, boolean selected) {
        try {
            List<CartItem> cartItems = getCartItems();
            for (CartItem item : cartItems) {
                if (item.getBook().getId().equals(bookId)) {
                    item.setSelected(selected);
                    saveCartItems(cartItems);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error setting item selection", e);
            return false;
        }
    }
    
    // Select all items
    public void selectAllItems(boolean selected) {
        try {
            List<CartItem> cartItems = getCartItems();
            for (CartItem item : cartItems) {
                item.setSelected(selected);
            }
            saveCartItems(cartItems);
        } catch (Exception e) {
            Log.e(TAG, "Error selecting all items", e);
        }
    }
    
    // Get selected items
    public List<CartItem> getSelectedItems() {
        List<CartItem> allItems = getCartItems();
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : allItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }
    
    // Get total price of selected items
    public int getSelectedItemsTotal() {
        List<CartItem> selectedItems = getSelectedItems();
        int total = 0;
        for (CartItem item : selectedItems) {
            total += item.getTotalPrice();
        }
        return total;
    }
    
    // Get count of selected items
    public int getSelectedItemsCount() {
        List<CartItem> selectedItems = getSelectedItems();
        int count = 0;
        for (CartItem item : selectedItems) {
            count += item.getQuantity();
        }
        return count;
    }
    
    // Remove selected items
    public void removeSelectedItems() {
        try {
            List<CartItem> cartItems = getCartItems();
            cartItems.removeIf(CartItem::isSelected);
            saveCartItems(cartItems);
            Log.d(TAG, "Removed selected items from cart");
        } catch (Exception e) {
            Log.e(TAG, "Error removing selected items", e);
        }
    }
}
