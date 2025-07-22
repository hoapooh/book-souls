package com.example.book_souls_project.api.repository;

import android.content.Context;

import com.example.book_souls_project.api.service.ReviewService;
import com.example.book_souls_project.api.types.review.ReviewCreateRequest;
import com.example.book_souls_project.api.types.review.ReviewCreateResponse;
import com.example.book_souls_project.api.types.review.ReviewListResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewRepository extends BaseRepository {
    private ReviewService reviewService;

    public ReviewRepository(Context context) {
        super(context);
        this.reviewService = apiClient.getRetrofit().create(ReviewService.class);
    }

    /**
     * Get reviews for a specific book
     * @param bookId The ID of the book
     * @param callback Callback for handling the response
     */
    public void getBookReviews(String bookId, ReviewListCallback callback) {
        Call<ReviewListResponse> call = reviewService.getBookReviews(bookId);
        
        call.enqueue(new Callback<ReviewListResponse>() {
            @Override
            public void onResponse(Call<ReviewListResponse> call, Response<ReviewListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMessage = "Failed to load reviews. Code: " + response.code() + ", Message: " + response.message();
                    
                    // Try to get error body
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMessage += " Error: " + errorBody;
                        }
                    } catch (Exception e) {
                        // Ignore error body reading issues
                    }
                    
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ReviewListResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Create a new review for a book
     * @param request The review creation request
     * @param callback Callback for handling the response
     */
    public void createReview(ReviewCreateRequest request, ReviewCreateCallback callback) {
        Call<ReviewCreateResponse> call = reviewService.createReview(request);

        call.enqueue(new Callback<ReviewCreateResponse>() {
            @Override
            public void onResponse(Call<ReviewCreateResponse> call, Response<ReviewCreateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message());
                }
            }

            @Override
            public void onFailure(Call<ReviewCreateResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Callback interfaces
    public interface ReviewListCallback {
        void onSuccess(ReviewListResponse response);
        void onError(String error);
    }

    public interface ReviewCreateCallback {
        void onSuccess(ReviewCreateResponse response);
        void onError(String error);
    }
}
