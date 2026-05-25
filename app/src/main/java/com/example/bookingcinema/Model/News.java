package com.example.bookingcinema.Model;

public class News {
    private String title;
    private String description;
    private String imageUrl;
    private String expiresAt;
    private int imageResId;

    public News() {}

    public News(String title, String description, int imageResId) {
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
    }

    public String getTitle() { return title == null ? "" : title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description == null ? "" : description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl == null ? "" : imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getExpiresAt() { return expiresAt == null ? "" : expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
}
