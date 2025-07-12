package com.example.book_souls_project.api.types.user;

import com.google.gson.annotations.SerializedName;

public class EditProfileRequest {
    @SerializedName("FullName")
    private String fullName;
    
    @SerializedName("PhoneNumber")
    private String phoneNumber;
    
    @SerializedName("Gender")
    private String gender;
    
    // Use individual address fields with exact names as expected by the backend
    @SerializedName("Address.Street")
    private String addressStreet;
    
    @SerializedName("Address.Ward")
    private String addressWard;
    
    @SerializedName("Address.District")
    private String addressDistrict;
    
    @SerializedName("Address.City")
    private String addressCity;
    
    @SerializedName("Address.Country")
    private String addressCountry;
    
    @SerializedName("Avatar")
    private String avatar; // Base64 encoded image or URL

    // Constructors
    public EditProfileRequest() {}

    public EditProfileRequest(String fullName, String phoneNumber, String gender,
                             String addressStreet, String addressWard, String addressDistrict,
                             String addressCity, String addressCountry) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.addressStreet = addressStreet;
        this.addressWard = addressWard;
        this.addressDistrict = addressDistrict;
        this.addressCity = addressCity;
        this.addressCountry = addressCountry;
    }

    // Getters
    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getGender() {
        return gender;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public String getAddressWard() {
        return addressWard;
    }

    public String getAddressDistrict() {
        return addressDistrict;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public String getAvatar() {
        return avatar;
    }

    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public void setAddressWard(String addressWard) {
        this.addressWard = addressWard;
    }

    public void setAddressDistrict(String addressDistrict) {
        this.addressDistrict = addressDistrict;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
