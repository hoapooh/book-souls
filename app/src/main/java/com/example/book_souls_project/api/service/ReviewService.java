package com.example.book_souls_project.api.service;

import com.example.book_souls_project.api.types.review.ReviewListResponse;
import com.example.book_souls_project.api.types.review.ReviewCreateRequest;
import com.example.book_souls_project.api.types.review.ReviewCreateResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Service interface for Review API endpoints
 */
public interface ReviewService {
    
    /**
     * Get reviews for a specific book
     * @param bookId The ID of the book to get reviews for
     * @return Call object for the API request
     */
    @GET("reviews")
    Call<ReviewListResponse> getBookReviews(@Query("BookId") String bookId);
    
    /**
     * Create a new review for a book
     * @param request The review creation request
     * @return Call object for the API request
     */
    @POST("reviews")
    Call<ReviewCreateResponse> createReview(@Body ReviewCreateRequest request);
}
