package com.example.book_souls_project.api.types.book;

import java.util.List;

public class BookListResponse {
    private String message;
    private Result result;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Result getResult() { return result; }
    public void setResult(Result result) { this.result = result; }

    public static class Result {
        private List<Book> items;
        private int totalCount;

        public List<Book> getItems() { return items; }
        public void setItems(List<Book> items) { this.items = items; }
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    }
}
