package com.example.book_souls_project.api.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.book_souls_project.api.service.PublisherService;
import com.example.book_souls_project.api.types.publisher.Publisher;
import com.example.book_souls_project.api.types.publisher.PublisherDetailResponse;
import com.example.book_souls_project.api.types.publisher.PublisherListResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublisherRepository extends BaseRepository {
    private static final String TAG = "PublisherRepository";
    private PublisherService publisherService;

    public PublisherRepository(Context context) {
        super(context);
        this.publisherService = apiClient.getRetrofit().create(PublisherService.class);
    }

    public LiveData<PublisherListResponse.PublisherResult> getPublishers(int pageNumber, int pageSize) {
        MutableLiveData<PublisherListResponse.PublisherResult> publishersLiveData = new MutableLiveData<>();
        
        Call<PublisherListResponse> call = publisherService.getPublishers(pageNumber, pageSize);
        call.enqueue(new Callback<PublisherListResponse>() {
            @Override
            public void onResponse(Call<PublisherListResponse> call, Response<PublisherListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    publishersLiveData.setValue(response.body().getResult());
                } else {
                    Log.e(TAG, "Failed to get publishers: " + response.code());
                    publishersLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<PublisherListResponse> call, Throwable t) {
                Log.e(TAG, "Error getting publishers", t);
                publishersLiveData.setValue(null);
            }
        });

        return publishersLiveData;
    }

    public LiveData<Publisher> getPublisherById(String publisherId) {
        MutableLiveData<Publisher> publisherLiveData = new MutableLiveData<>();
        
        Call<PublisherDetailResponse> call = publisherService.getPublisherById(publisherId);
        call.enqueue(new Callback<PublisherDetailResponse>() {
            @Override
            public void onResponse(Call<PublisherDetailResponse> call, Response<PublisherDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    publisherLiveData.setValue(response.body().getResult());
                } else {
                    Log.e(TAG, "Failed to get publisher: " + response.code());
                    publisherLiveData.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<PublisherDetailResponse> call, Throwable t) {
                Log.e(TAG, "Error getting publisher", t);
                publisherLiveData.setValue(null);
            }
        });

        return publisherLiveData;
    }
}
