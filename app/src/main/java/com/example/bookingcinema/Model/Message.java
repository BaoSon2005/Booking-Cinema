package com.example.bookingcinema.Model;

public class Message {
    private String senderId;
    private String receiverId;
    private String content;
    private long timestamp;

    public Message() {} // required for Firestore

    public Message(String senderId, String receiverId, String content, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
}
