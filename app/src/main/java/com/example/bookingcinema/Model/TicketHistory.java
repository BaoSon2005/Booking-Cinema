package com.example.bookingcinema.Model;

public class TicketHistory {
    private String movieTitle;
    private String cinema;
    private String showtime;
    private String seats;
    private int totalPrice;

    public TicketHistory(String movieTitle, String cinema, String showtime, String seats, int totalPrice) {
        this.movieTitle = movieTitle;
        this.cinema = cinema;
        this.showtime = showtime;
        this.seats = seats;
        this.totalPrice = totalPrice;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getCinema() {
        return cinema;
    }

    public String getShowtime() {
        return showtime;
    }

    public String getSeats() {
        return seats;
    }

    public int getTotalPrice() {
        return totalPrice;
    }
}
