package com.example.bookingcinema.Model;

import java.io.Serializable;

public class Movie implements Serializable {
    private String id;
    private String title;
    private String description;
    private int imageResId;  // Dùng int để lưu drawable resource ID

    public Movie() {
        // Required for Firebase hoặc serialization
    }

    // Constructor đầy đủ
    public Movie(String id, String title, String description, int imageResId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
    }

    // Constructor không có ảnh, gán ảnh mặc định
    public Movie(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageResId = android.R.drawable.ic_menu_report_image; // Ảnh mặc định nếu không có
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
}
