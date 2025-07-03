package com.example.book_souls_project.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class MapTestHelper {
    private static final String TAG = "MapTestHelper";
    
    public static void validateGoogleMapsSetup(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            
            if (bundle != null) {
                String apiKey = bundle.getString("com.google.android.geo.API_KEY");
                if (apiKey != null && !apiKey.isEmpty()) {
                    Log.d(TAG, "Google Maps API Key found: " + apiKey.substring(0, Math.min(10, apiKey.length())) + "...");
                    Log.d(TAG, "API Key length: " + apiKey.length());
                } else {
                    Log.e(TAG, "Google Maps API Key not found in manifest!");
                }
            } else {
                Log.e(TAG, "No meta-data found in manifest!");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package not found", e);
        } catch (Exception e) {
            Log.e(TAG, "Error checking API key", e);
        }
    }
    
    public static void logMapSetupInfo() {
        Log.d(TAG, "=== Google Maps Setup Information ===");
        Log.d(TAG, "Google Play Services version should be available");
        Log.d(TAG, "Location permissions should be requested at runtime");
        Log.d(TAG, "Check logcat for 'Authorization failure' or 'API key' errors");
        Log.d(TAG, "=======================================");
    }
}
