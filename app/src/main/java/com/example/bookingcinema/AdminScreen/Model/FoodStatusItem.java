package com.example.bookingcinema.AdminScreen.Model;

import com.google.firebase.firestore.DocumentSnapshot;

public class FoodStatusItem {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private long price;
    private boolean available = true;

    public FoodStatusItem() {
    }

    public static FoodStatusItem fromSnapshot(DocumentSnapshot doc) {
        FoodStatusItem item = new FoodStatusItem();
        item.id = doc.getId();
        item.name = firstText(doc, "name", "tenMon", "title");
        item.description = firstText(doc, "description", "moTa", "subtitle");
        item.imageUrl = firstText(doc, "imageUrl", "posterUrl", "image");
        Long priceValue = firstLong(doc, "price", "gia", "amount");
        item.price = priceValue == null ? 0L : priceValue;
        Boolean isAvailable = doc.getBoolean("isAvailable");
        if (isAvailable == null) {
            isAvailable = doc.getBoolean("available");
        }
        item.available = isAvailable == null || isAvailable;
        return item;
    }

    private static String firstText(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            String value = doc.getString(key);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private static Long firstLong(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            Long value = doc.getLong(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public String getId() {
        return id == null ? "" : id;
    }

    public String getName() {
        return name == null || name.isEmpty() ? "Combo CINE-LUXE" : name;
    }

    public String getDescription() {
        return description == null || description.isEmpty() ? "Combo bắp nước cao cấp tại quầy." : description;
    }

    public String getImageUrl() {
        return imageUrl == null ? "" : imageUrl;
    }

    public long getPrice() {
        return price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
