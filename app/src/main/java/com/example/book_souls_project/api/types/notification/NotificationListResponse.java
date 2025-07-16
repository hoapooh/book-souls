package com.example.book_souls_project.api.types.notification;

import java.util.List;

public class NotificationListResponse {
    private Result result;
    public Result getResult() { return result; }
    public void setResult(Result result) { this.result = result; }

    public static class Result {
        private List<NotificationResponse> items;
        private int totalCount;
        public List<NotificationResponse> getItems() { return items; }
        public void setItems(List<NotificationResponse> items) { this.items = items; }
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    }
}
