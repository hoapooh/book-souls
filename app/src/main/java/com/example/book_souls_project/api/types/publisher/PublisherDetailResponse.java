package com.example.book_souls_project.api.types.publisher;

public class PublisherDetailResponse {
    private String message;
    private Publisher result;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Publisher getResult() {
        return result;
    }

    public void setResult(Publisher result) {
        this.result = result;
    }
}
