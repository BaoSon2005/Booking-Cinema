package com.example.bookingcinema.Model;

import java.io.Serializable;

public class Showtime implements Serializable {
    private String movieTitle;
    private String cinema;
    private String time;
    private boolean confirmed;

    public Showtime(String movieTitle, String cinema, String time, boolean confirmed) {
        this.movieTitle = movieTitle;
        this.cinema = cinema;
        this.time = time;
        this.confirmed = confirmed;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getCinema() {
        return cinema;
    }

    public void setCinema(String cinema) {
        this.cinema = cinema;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
}
