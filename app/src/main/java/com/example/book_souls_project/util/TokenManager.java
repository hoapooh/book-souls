package com.example.book_souls_project.util;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "BookSoulsPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_AVATAR = "user_avatar";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save user session
    public void saveUserSession(String accessToken, String userId, String fullName, String role, String avatar) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, fullName);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_AVATAR, avatar);
        editor.apply();
    }

    // Get access token
    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    // Get user info
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, null);
    }

    public String getUserAvatar() {
        return sharedPreferences.getString(KEY_USER_AVATAR, null);
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return getAccessToken() != null && !getAccessToken().isEmpty();
    }

    // Clear user session
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
