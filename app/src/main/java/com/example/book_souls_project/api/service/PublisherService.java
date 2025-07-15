package com.example.book_souls_project.api.service;

import com.example.book_souls_project.api.types.publisher.PublisherDetailResponse;
import com.example.book_souls_project.api.types.publisher.PublisherListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PublisherService {
    @GET("publishers")
    Call<PublisherListResponse> getPublishers(
            @Query("pageNumber") int pageNumber,
            @Query("pageSize") int pageSize
    );

    @GET("publishers/{id}")
    Call<PublisherDetailResponse> getPublisherById(@Path("id") String publisherId);
}
