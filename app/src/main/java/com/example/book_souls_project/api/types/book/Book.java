package com.example.book_souls_project.api.types.book;

import java.util.List;

public class Book {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String publisherId;
    private List<String> categoryIds;
    private int releaseYear;
    private boolean isStricted;
    private int price;
    private int stock;
    private String image;
    private float rating;
    private int ratingCount;
    private String createdAt;
    private String updatedAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getPublisherId() { return publisherId; }
    public void setPublisherId(String publisherId) { this.publisherId = publisherId; }

    public List<String> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<String> categoryIds) { this.categoryIds = categoryIds; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public boolean isStricted() { return isStricted; }
    public void setStricted(boolean stricted) { isStricted = stricted; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
