package com.example.bookingcinema.Model;

import java.io.Serializable;

public class Movie implements Serializable {
    private String id;
    private String title;
    private String originalTitle;
    private String description;
    private String posterUrl;
    private String bannerUrl;
    private String trailerUrl;
    private String genre;
    private String duration;
    private String ageLimit;
    private String status;
    private String releaseDate;
    private double rating;
    private int imageResId;
    private int basePrice;
    private boolean deXuat;
    private boolean isRecommended;

    public Movie() {
        imageResId = 0;
        basePrice = 90000;
        status = "Đang chiếu";
    }

    public Movie(String id, String title, String description, int imageResId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
        this.basePrice = 90000;
        this.status = "Đang chiếu";
    }

    public Movie(String id, String title, String description) {
        this(id, title, description, 0);
    }

    public String getId() { return id == null ? "" : id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title == null ? "" : title; }
    public void setTitle(String title) { this.title = title; }
    public String getOriginalTitle() { return originalTitle == null ? "" : originalTitle; }
    public void setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; }
    public String getDescription() { return description == null ? "" : description; }
    public void setDescription(String description) { this.description = description; }
    public String getPosterUrl() { return posterUrl == null ? "" : posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getBannerUrl() { return bannerUrl == null ? "" : bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
    public String getTrailerUrl() { return trailerUrl == null ? "" : trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public String getGenre() { return genre == null ? "" : genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getDuration() { return duration == null ? "" : duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getAgeLimit() { return ageLimit == null ? "" : ageLimit; }
    public void setAgeLimit(String ageLimit) { this.ageLimit = ageLimit; }
    public String getStatus() { return status == null || status.trim().isEmpty() ? "Đang chiếu" : status; }
    public void setStatus(String status) { this.status = status; }
    public String getReleaseDate() { return releaseDate == null ? "" : releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public int getBasePrice() { return basePrice <= 0 ? 90000 : basePrice; }
    public void setBasePrice(int basePrice) { this.basePrice = basePrice; }
    public boolean isDeXuat() { return deXuat; }
    public void setDeXuat(boolean deXuat) { this.deXuat = deXuat; }
    public boolean isRecommended() { return isRecommended; }
    public void setRecommended(boolean recommended) { isRecommended = recommended; }
}
