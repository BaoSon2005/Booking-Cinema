package com.example.bookingcinema.AdminScreen.Model;

public class AdminAlert {
    private String id;
    private String title;
    private String message;
    private String severity;
    private long createdAtMillis;

    public AdminAlert() {
    }

    public String getId() {
        return id == null ? "" : id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title == null ? "Cảnh báo hoạt động" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message == null ? "Cần kiểm tra vận hành trong hệ thống rạp." : message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity == null ? "warning" : severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public void setCreatedAtMillis(long createdAtMillis) {
        this.createdAtMillis = createdAtMillis;
    }

    public boolean isCritical() {
        String value = getSeverity().toLowerCase();
        return value.contains("critical") || value.contains("khẩn") || value.contains("do") || value.contains("red");
    }
}
