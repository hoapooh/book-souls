package com.example.book_souls_project.api.types.book;

public class BookDetailResponse {
    private String message;
    private Book result;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Book getResult() { return result; }
    public void setResult(Book result) { this.result = result; }
}
