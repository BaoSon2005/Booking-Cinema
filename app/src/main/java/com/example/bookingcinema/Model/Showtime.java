package com.example.bookingcinema.Model;

import java.io.Serializable;

public class Showtime implements Serializable {
    private String id;
    private String movieId;
    private String movieTitle;
    private String cinema;
    private String date;
    private String time;
    private String room;

    public Showtime() {}

    public String getId() { return id == null ? "" : id; }
    public void setId(String id) { this.id = id; }
    public String getMovieId() { return movieId == null ? "" : movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public String getMovieTitle() { return movieTitle == null ? "" : movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public String getCinema() { return cinema == null ? "" : cinema; }
    public void setCinema(String cinema) { this.cinema = cinema; }
    public String getDate() { return date == null ? "" : date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time == null ? "" : time; }
    public void setTime(String time) { this.time = time; }
    public String getRoom() { return room == null ? "" : room; }
    public void setRoom(String room) { this.room = room; }
}
