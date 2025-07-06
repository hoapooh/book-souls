package com.example.book_souls_project.api.types.publisher;

import java.util.List;

public class PublisherListResponse {
    private String message;
    private PublisherResult result;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PublisherResult getResult() {
        return result;
    }

    public void setResult(PublisherResult result) {
        this.result = result;
    }

    public static class PublisherResult {
        private List<Publisher> items;
        private int totalCount;

        public List<Publisher> getItems() {
            return items;
        }

        public void setItems(List<Publisher> items) {
            this.items = items;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }
    }
}
