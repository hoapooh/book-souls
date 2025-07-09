package com.example.book_souls_project.util;

import android.content.Context;

import com.example.book_souls_project.R;

public class Constants {
    // API Endpoints
    public static final class Api {
        public static String getBaseUrl(Context context) {
            return context.getString(R.string.api_client_url);
        }

        public static final String BASE_URL = String.valueOf(R.string.api_client_url);
        public static final String AUTHENTICATION = "authentication";
        public static final String BOOKS = "books";
        public static final String USERS = "users";
    }

    // SharedPreferences Keys
    public static final class Prefs {
        public static final String PREF_NAME = "BookSoulsPrefs";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "user_name";
        public static final String USER_ROLE = "user_role";
        public static final String USER_AVATAR = "user_avatar";
    }

    // Network Timeouts
    public static final class Network {
        public static final int CONNECT_TIMEOUT = 30; // seconds
        public static final int READ_TIMEOUT = 30; // seconds
        public static final int WRITE_TIMEOUT = 30; // seconds
    }

    // User Roles
    public static final class Roles {
        public static final String ADMIN = "admin";
        public static final String USER = "user";
        public static final String MODERATOR = "moderator";
    }
}
