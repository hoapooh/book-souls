package com.example.book_souls_project.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.book_souls_project.R;

import java.net.URLDecoder;

public class ImageUtils {
    
    /**
     * Load image with proper URL decoding and fallback URL handling
     */
    public static void loadProfileImage(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_user);
            return;
        }

        try {
            // Handle double-encoded URLs by decoding multiple times if needed
            String decodedUrl = cleanImageUrl(imageUrl);
            
            Log.d("ImageUtils", "Original URL: " + imageUrl);
            Log.d("ImageUtils", "Decoded URL: " + decodedUrl);
            
            RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE); // Don't cache to test fresh requests

            // Try to load the image with a simple approach
            Glide.with(context)
                .load(decodedUrl)
                .apply(options)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, 
                                              com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                              boolean isFirstResource) {
                        Log.e("ImageUtils", "Failed to load image from: " + model, e);
                        
                        // Try alternative URL formats if this is a Cloudinary URL
                        if (decodedUrl.contains("cloudinary")) {
                            String fallbackUrl = createSimpleCloudinaryUrl(decodedUrl);
                            if (fallbackUrl != null && !fallbackUrl.equals(decodedUrl)) {
                                // Use Handler to post the fallback load to the main thread
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    Log.d("ImageUtils", "Trying fallback URL: " + fallbackUrl);
                                    Glide.with(context)
                                        .load(fallbackUrl)
                                        .apply(options)
                                        .into(imageView);
                                });
                                return true; // We're handling the error
                            }
                        }
                        return false; // Let Glide show error drawable
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, 
                                                 com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                                 com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        Log.d("ImageUtils", "Successfully loaded image from: " + model);
                        return false;
                    }
                })
                .into(imageView);
                
        } catch (Exception e) {
            // If everything fails, just show default image
            Log.e("ImageUtils", "Failed to load image: " + imageUrl, e);
            imageView.setImageResource(R.drawable.ic_user);
        }
    }
    
    /**
     * Alternative method for loading images with Handler-based fallback
     * Use this if the error() method approach doesn't work as expected
     */
    public static void loadProfileImageWithHandlerFallback(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_user);
            return;
        }

        try {
            String decodedUrl = cleanImageUrl(imageUrl);
            
            Log.d("ImageUtils", "Loading with Handler fallback - Original URL: " + imageUrl);
            Log.d("ImageUtils", "Loading with Handler fallback - Decoded URL: " + decodedUrl);
            
            RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE);

            Glide.with(context)
                .load(decodedUrl)
                .apply(options)
                .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, 
                                              com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                              boolean isFirstResource) {
                        Log.e("ImageUtils", "Failed to load image from: " + decodedUrl, e);
                        
                        // Try alternative URL formats if this is a Cloudinary URL
                        if (decodedUrl.contains("cloudinary")) {
                            String fallbackUrl = createSimpleCloudinaryUrl(decodedUrl);
                            if (fallbackUrl != null && !fallbackUrl.equals(decodedUrl)) {
                                // Use Handler to post the fallback load to the main thread
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    Log.d("ImageUtils", "Trying fallback URL with Handler: " + fallbackUrl);
                                    Glide.with(context)
                                        .load(fallbackUrl)
                                        .apply(options)
                                        .into(imageView);
                                });
                                return true; // We're handling the error
                            }
                        }
                        return false; // Let Glide show error drawable
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, 
                                                 com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                                 com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        Log.d("ImageUtils", "Successfully loaded image from: " + decodedUrl);
                        return false;
                    }
                })
                .into(imageView);
                
        } catch (Exception e) {
            Log.e("ImageUtils", "Failed to load image: " + imageUrl, e);
            imageView.setImageResource(R.drawable.ic_user);
        }
    }
    
    /**
     * Create a simplified Cloudinary URL by removing transformations
     */
    private static String createSimpleCloudinaryUrl(String decodedUrl) {
        try {
            if (decodedUrl.contains("/upload/")) {
                // Remove any transformations between /upload/ and the image name
                String[] parts = decodedUrl.split("/upload/");
                if (parts.length >= 2) {
                    String afterUpload = parts[1];
                    
                    // Handle versioned URLs (v1234567890/path)
                    if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
                        String[] versionParts = afterUpload.split("/", 2);
                        if (versionParts.length >= 2) {
                            afterUpload = versionParts[1]; // Skip version
                        }
                    }
                    
                    // Find the actual image name (after the last /)
                    String[] pathParts = afterUpload.split("/");
                    String imageName = pathParts[pathParts.length - 1];
                    
                    // Try different formats
                    String baseCloudinaryUrl = parts[0] + "/upload/";
                    String simpleUrl = baseCloudinaryUrl + imageName;
                    
                    Log.d("ImageUtils", "Created simple Cloudinary URL: " + simpleUrl);
                    
                    // If the original image has a complex path, also try with just the base64-like part
                    if (imageName.contains("+") || imageName.contains("=")) {
                        // This looks like it might be a base64 encoded name, try removing file extension and re-adding
                        if (imageName.contains(".")) {
                            String nameWithoutExt = imageName.substring(0, imageName.lastIndexOf("."));
                            String extension = imageName.substring(imageName.lastIndexOf("."));
                            // Try different extensions
                            String alternativeUrl = baseCloudinaryUrl + nameWithoutExt + ".jpg";
                            Log.d("ImageUtils", "Alternative Cloudinary URL: " + alternativeUrl);
                            return alternativeUrl;
                        }
                    }
                    
                    return simpleUrl;
                }
            }
        } catch (Exception e) {
            Log.e("ImageUtils", "Failed to create simple Cloudinary URL", e);
        }
        return null;
    }
    
    /**
     * Clean and decode image URL - handles multiple levels of encoding
     */
    private static String cleanImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return imageUrl;
        }
        
        try {
            String decodedUrl = imageUrl;
            
            // Decode multiple times to handle double/triple encoding
            int maxDecodeAttempts = 3;
            for (int i = 0; i < maxDecodeAttempts && decodedUrl.contains("%"); i++) {
                try {
                    String previousUrl = decodedUrl;
                    decodedUrl = URLDecoder.decode(decodedUrl, "UTF-8");
                    
                    // If URL didn't change, break to avoid infinite loop
                    if (decodedUrl.equals(previousUrl)) {
                        break;
                    }
                    
                    Log.d("ImageUtils", "Decode attempt " + (i + 1) + ": " + decodedUrl);
                } catch (Exception e) {
                    Log.w("ImageUtils", "Failed to decode URL at attempt " + (i + 1), e);
                    break;
                }
            }
            
            return decodedUrl;
        } catch (Exception e) {
            Log.e("ImageUtils", "Failed to clean image URL", e);
            return imageUrl;
        }
    }
    
    /**
     * Clear image cache for Glide
     */
    public static void clearImageCache(Context context) {
        Glide.get(context).clearMemory();
        // Clear disk cache should be done on background thread
        new Thread(() -> Glide.get(context).clearDiskCache()).start();
    }
    
    /**
     * Test method to debug URL decoding - can be called from fragments for testing
     */
    public static void debugAvatarUrl(String originalUrl) {
        if (originalUrl == null) {
            Log.d("ImageUtils", "DEBUG: Avatar URL is null");
            return;
        }
        
        Log.d("ImageUtils", "DEBUG: Original avatar URL: " + originalUrl);
        Log.d("ImageUtils", "DEBUG: URL length: " + originalUrl.length());
        
        String cleanedUrl = cleanImageUrl(originalUrl);
        Log.d("ImageUtils", "DEBUG: Cleaned avatar URL: " + cleanedUrl);
        
        if (cleanedUrl.contains("cloudinary")) {
            String simpleUrl = createSimpleCloudinaryUrl(cleanedUrl);
            Log.d("ImageUtils", "DEBUG: Simple Cloudinary URL: " + simpleUrl);
        }
    }
    
    /**
     * Simple profile image loading without URL decoding - works like BookDetailFragment
     */
    public static void loadProfileImageSimple(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_user);
            return;
        }

        Log.d("ImageUtils", "Loading profile image (simple): " + imageUrl);
        
        RequestOptions options = new RequestOptions()
            .placeholder(R.drawable.ic_user)
            .error(R.drawable.ic_user)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL); // Use caching like BookDetailFragment

        // Load image directly without URL decoding - same as BookDetailFragment
        Glide.with(context)
            .load(imageUrl)
            .apply(options)
            .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                @Override
                public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, 
                                          com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                          boolean isFirstResource) {
                    Log.e("ImageUtils", "Failed to load profile image from: " + model, e);
                    return false; // Let Glide show error drawable
                }

                @Override
                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, 
                                             com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, 
                                             com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                    Log.d("ImageUtils", "Successfully loaded profile image from: " + model);
                    return false;
                }
            })
            .into(imageView);
    }
}
