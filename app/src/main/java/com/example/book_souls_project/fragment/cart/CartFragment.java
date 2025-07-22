package com.example.book_souls_project.fragment.cart;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.book_souls_project.R;
import com.example.book_souls_project.MainActivity;
import com.example.book_souls_project.adapter.CartItemAdapter;
import com.example.book_souls_project.util.CartManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment implements CartItemAdapter.OnCartItemActionListener {

    private CartViewModel mViewModel;
    private CartManager cartManager;
    private CartItemAdapter adapter;
    
    // UI Components
    private RecyclerView recyclerViewCartItems;
    private LinearLayout emptyCartState;
    private CheckBox checkboxSelectAll;
    private TextView textItemCount;
    private TextView textSelectedCount;
    private TextView textSubtotal;
    private TextView textTotal;
    private MaterialButton buttonCheckout;
    private MaterialButton buttonDeleteSelected;
    private ImageButton buttonClearCart;
    private MaterialButton buttonBrowseBooks;

    public static CartFragment newInstance() {
        return new CartFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Apply window insets to handle system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        
        cartManager = new CartManager(requireContext());
        mViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        
        loadCartItems();
    }

    private void initViews(View view) {
        recyclerViewCartItems = view.findViewById(R.id.recyclerViewCartItems);
        emptyCartState = view.findViewById(R.id.emptyCartState);
        checkboxSelectAll = view.findViewById(R.id.checkboxSelectAll);
        textItemCount = view.findViewById(R.id.textItemCount);
        textSelectedCount = view.findViewById(R.id.textSelectedCount);
        textSubtotal = view.findViewById(R.id.textSubtotal);
        textTotal = view.findViewById(R.id.textTotal);
        buttonCheckout = view.findViewById(R.id.buttonCheckout);
        buttonDeleteSelected = view.findViewById(R.id.buttonDeleteSelected);
        buttonClearCart = view.findViewById(R.id.buttonClearCart);
        buttonBrowseBooks = view.findViewById(R.id.buttonBrowseBooks);
    }

    private void setupRecyclerView() {
        adapter = new CartItemAdapter(this);
        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewCartItems.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Select All Checkbox
        checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartManager.selectAllItems(isChecked);
            loadCartItems(); // Refresh to update UI
        });

        // Checkout Button
        buttonCheckout.setOnClickListener(v -> {
            List<CartManager.CartItem> selectedItems = cartManager.getSelectedItems();
            if (!selectedItems.isEmpty()) {
                // Navigate to checkout fragment with selected items
                Bundle bundle = new Bundle();
                // You can pass selected items data here
                Navigation.findNavController(v).navigate(R.id.action_cartFragment_to_checkoutFragment, bundle);
            } else {
                Toast.makeText(getContext(), "Please select items to checkout", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete Selected Button
        buttonDeleteSelected.setOnClickListener(v -> {
            int selectedCount = cartManager.getSelectedItemsCount();
            if (selectedCount > 0) {
                cartManager.removeSelectedItems();
                loadCartItems();
                Toast.makeText(getContext(), "Removed " + selectedCount + " items", Toast.LENGTH_SHORT).show();
            }
        });

        // Clear Cart Button
        buttonClearCart.setOnClickListener(v -> {
            cartManager.clearCart();
            loadCartItems();
            Toast.makeText(getContext(), "Cart cleared", Toast.LENGTH_SHORT).show();
        });

        // Browse Books Button
        buttonBrowseBooks.setOnClickListener(v -> {
            // Navigate to home page
            Navigation.findNavController(v).navigate(R.id.navigation_home);
        });
    }

    private void loadCartItems() {
        List<CartManager.CartItem> cartItems = cartManager.getCartItems();
        
        if (cartItems.isEmpty()) {
            showEmptyState();
        } else {
            showCartItems(cartItems);
        }
        
        updateUI();
    }

    private void showEmptyState() {
        recyclerViewCartItems.setVisibility(View.GONE);
        emptyCartState.setVisibility(View.VISIBLE);
    }

    private void showCartItems(List<CartManager.CartItem> cartItems) {
        recyclerViewCartItems.setVisibility(View.VISIBLE);
        emptyCartState.setVisibility(View.GONE);
        adapter.updateCartItems(cartItems);
    }

    private void updateUI() {
        List<CartManager.CartItem> allItems = cartManager.getCartItems();
        List<CartManager.CartItem> selectedItems = cartManager.getSelectedItems();
        
        // Update item counts
        textItemCount.setText(allItems.size() + " items in cart");
        textSelectedCount.setText(selectedItems.size() + " selected");
        
        // Update select all checkbox
        checkboxSelectAll.setOnCheckedChangeListener(null);
        if (allItems.isEmpty()) {
            checkboxSelectAll.setChecked(false);
            checkboxSelectAll.setEnabled(false);
        } else {
            checkboxSelectAll.setEnabled(true);
            checkboxSelectAll.setChecked(selectedItems.size() == allItems.size());
        }
        checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartManager.selectAllItems(isChecked);
            loadCartItems();
        });
        
        // Calculate prices for selected items
        int selectedTotal = cartManager.getSelectedItemsTotal();
        int finalTotal = selectedTotal;
        
        // Format currency
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textSubtotal.setText(currencyFormat.format(selectedTotal));
        textTotal.setText(currencyFormat.format(finalTotal));
        
        // Update button states
        boolean hasSelected = !selectedItems.isEmpty();
        buttonCheckout.setEnabled(hasSelected);
        buttonCheckout.setText(hasSelected ? 
            "Checkout (" + selectedItems.size() + " items)" : 
            "Select items to checkout");
        
        buttonDeleteSelected.setVisibility(hasSelected ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemSelectionChanged(String bookId, boolean isSelected) {
        cartManager.setItemSelected(bookId, isSelected);
        updateUI();
    }

    @Override
    public void onQuantityChanged(String bookId, int newQuantity) {
        cartManager.updateQuantity(bookId, newQuantity);
        loadCartItems();
    }

    @Override
    public void onItemRemoved(String bookId) {
        cartManager.removeFromCart(bookId);
        loadCartItems();
        Toast.makeText(getContext(), "Item removed from cart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCartItems(); // Refresh cart when returning to fragment
    }
}