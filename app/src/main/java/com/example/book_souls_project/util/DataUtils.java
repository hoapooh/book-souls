package com.example.book_souls_project.util;

public class DataUtils {
    
    /**
     * Clean string by removing extra quotes if they exist
     */
    public static String cleanString(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // Remove leading and trailing quotes if they exist
        String cleaned = input.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() > 1) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        
        return cleaned;
    }
    
    /**
     * Clean user profile data from extra quotes
     */
    public static void cleanUserProfile(com.example.book_souls_project.api.types.user.UserProfile userProfile) {
        if (userProfile == null) return;
        
        userProfile.setFullName(cleanString(userProfile.getFullName()));
        userProfile.setPhoneNumber(cleanString(userProfile.getPhoneNumber()));
        userProfile.setGender(cleanString(userProfile.getGender()));
        userProfile.setAvatar(cleanString(userProfile.getAvatar()));
        
        if (userProfile.getAddress() != null) {
            userProfile.getAddress().setStreet(cleanString(userProfile.getAddress().getStreet()));
            userProfile.getAddress().setWard(cleanString(userProfile.getAddress().getWard()));
            userProfile.getAddress().setDistrict(cleanString(userProfile.getAddress().getDistrict()));
            userProfile.getAddress().setCity(cleanString(userProfile.getAddress().getCity()));
            userProfile.getAddress().setCountry(cleanString(userProfile.getAddress().getCountry()));
        }
    }
}
