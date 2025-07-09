package com.example.book_souls_project.api.types.auth;

public class LoginResponseToken {
    private String accessToken;
    private String id;
    private String fullName;
    private String role;
    private String avatar;

    public String getAccessToken() {
        return accessToken;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public String getAvatar() {
        return avatar;
    }
}
