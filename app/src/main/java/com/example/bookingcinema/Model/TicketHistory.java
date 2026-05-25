package com.example.bookingcinema.Model;

public class TicketHistory {
    private String ticketCode;
    private String movieTitle;
    private String cinema;
    private String showtime;
    private String seats;
    private String status;
    private long createdAtMillis;
    private int totalPrice;

    public TicketHistory() {}

    public TicketHistory(String movieTitle, String cinema, String showtime, String seats, int totalPrice) {
        this.movieTitle = movieTitle;
        this.cinema = cinema;
        this.showtime = showtime;
        this.seats = seats;
        this.totalPrice = totalPrice;
        this.status = "Đã thanh toán";
    }

    public String getTicketCode() { return ticketCode == null ? "" : ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }
    public String getMovieTitle() { return movieTitle == null ? "" : movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public String getCinema() { return cinema == null ? "" : cinema; }
    public void setCinema(String cinema) { this.cinema = cinema; }
    public String getShowtime() { return showtime == null ? "" : showtime; }
    public void setShowtime(String showtime) { this.showtime = showtime; }
    public String getSeats() { return seats == null ? "" : seats; }
    public void setSeats(String seats) { this.seats = seats; }
    public String getStatus() { return status == null || status.trim().isEmpty() ? "Đã thanh toán" : status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAtMillis() { return createdAtMillis; }
    public void setCreatedAtMillis(long createdAtMillis) { this.createdAtMillis = createdAtMillis; }
    public int getTotalPrice() { return totalPrice; }
    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }
}
