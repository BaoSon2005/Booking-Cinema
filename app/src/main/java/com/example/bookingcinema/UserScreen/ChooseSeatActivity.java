package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.Model.Movie;
import com.example.bookingcinema.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChooseSeatActivity extends AppCompatActivity {

    private static final int ROWS = 8;
    private static final int COLS = 10;

    private GridLayout gridSeats;
    private TextView txtSeatPriceDetail, tvHoldCountdown, tvMovieTitle, tvCinemaTime;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private Movie movie;
    private String movieTitle, cinema, showtime, showtimeId;
    private final Seat[][] seats = new Seat[ROWS][COLS];
    private CountDownTimer holdTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_seat);

        db = FirebaseFirestore.getInstance();
        gridSeats = findViewById(R.id.gridSeats);
        txtSeatPriceDetail = findViewById(R.id.txtSeatPriceDetail);
        tvHoldCountdown = findViewById(R.id.tvHoldCountdown);
        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvCinemaTime = findViewById(R.id.tvCinemaTime);
        progressBar = findViewById(R.id.progressBar);
        Button btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(v -> continueToPayment());

        readIntent();
        tvMovieTitle.setText(movieTitle);
        tvCinemaTime.setText(cinema + " • " + showtime);
        initDefaultSeats();
        setupSeats();
        loadSeatsFromFirestore();
        startHoldTimer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (holdTimer != null) holdTimer.cancel();
    }

    private void readIntent() {
        Intent intent = getIntent();
        movie = (Movie) intent.getSerializableExtra("movie");
        movieTitle = movie != null ? movie.getTitle() : "Phim CINE-LUXE";
        cinema = firstNonEmpty(intent.getStringExtra("cinema"), "CINE-LUXE Landmark 81");
        showtime = firstNonEmpty(intent.getStringExtra("showtime"), "Hôm nay • 19:30 • H12");
        showtimeId = firstNonEmpty(intent.getStringExtra("showtimeId"), "fallback-" + movieTitle.replace(" ", "-").toLowerCase(Locale.ROOT));
    }

    private void initDefaultSeats() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                String code = getRowLabel(r) + (c + 1);
                String type = "Thường";
                int price = movie != null ? movie.getBasePrice() : 90000;
                if (r >= 2 && r <= 4) {
                    type = "VIP";
                    price += 25000;
                }
                if (r >= 6 && c >= 2 && c <= 7) {
                    type = "Couple";
                    price += 60000;
                }
                seats[r][c] = new Seat(code, type, price, (r + c) % 13 == 0);
            }
        }
    }

    private void loadSeatsFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("seats").whereEqualTo("showtimeId", showtimeId).get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) applySeatDocument(doc);
                    progressBar.setVisibility(View.GONE);
                    setupSeats();
                    updateSeatSummary();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    setupSeats();
                    toast("Không tải được sơ đồ ghế, đang dùng sơ đồ mẫu");
                });
    }

    private void applySeatDocument(DocumentSnapshot doc) {
        String code = firstNonEmpty(doc.getString("code"), doc.getString("seatCode"), doc.getId()).toUpperCase(Locale.ROOT);
        if (code.length() < 2) return;
        int row = code.charAt(0) - 'A';
        int col;
        try {
            col = Integer.parseInt(code.substring(1)) - 1;
        } catch (NumberFormatException e) {
            return;
        }
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return;
        Seat seat = seats[row][col];
        seat.type = firstNonEmpty(doc.getString("type"), doc.getString("category"), seat.type);
        Long price = doc.getLong("price");
        if (price != null && price > 0) seat.price = price.intValue();
        String status = firstNonEmpty(doc.getString("status"));
        Boolean booked = doc.getBoolean("booked");
        seat.booked = Boolean.TRUE.equals(booked) || status.equalsIgnoreCase("booked") || status.equalsIgnoreCase("sold") || status.equalsIgnoreCase("Đã đặt");
    }

    private void setupSeats() {
        gridSeats.removeAllViews();
        gridSeats.setColumnCount(COLS);
        gridSeats.setRowCount(ROWS);
        int seatSize = dp(34);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Seat seat = seats[r][c];
                TextView seatView = new TextView(this);
                seatView.setText(seat.code);
                seatView.setTextSize(10);
                seatView.setTypeface(Typeface.DEFAULT_BOLD);
                seatView.setGravity(Gravity.CENTER);
                seatView.setTextColor(seat.booked ? Color.parseColor("#777777") : Color.WHITE);
                seatView.setBackground(makeSeatBackground(seat));
                seatView.setEnabled(!seat.booked);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(r), GridLayout.spec(c));
                params.width = seatSize;
                params.height = seatSize;
                params.setMargins(dp(4), dp(4), dp(4), dp(4));
                seatView.setLayoutParams(params);
                final int row = r;
                final int col = c;
                seatView.setOnClickListener(v -> {
                    seats[row][col].selected = !seats[row][col].selected;
                    v.setBackground(makeSeatBackground(seats[row][col]));
                    updateSeatSummary();
                });
                gridSeats.addView(seatView);
            }
        }
    }

    private GradientDrawable makeSeatBackground(Seat seat) {
        int fill;
        int stroke;
        if (seat.booked) {
            fill = Color.parseColor("#242424");
            stroke = Color.parseColor("#44FFFFFF");
        } else if (seat.selected) {
            fill = Color.parseColor("#FF0033");
            stroke = Color.WHITE;
        } else if ("VIP".equalsIgnoreCase(seat.type)) {
            fill = Color.parseColor("#1AFFFFFF");
            stroke = Color.WHITE;
        } else if ("Couple".equalsIgnoreCase(seat.type)) {
            fill = Color.parseColor("#22FF0033");
            stroke = Color.parseColor("#FF0033");
        } else {
            fill = Color.parseColor("#1AFFFFFF");
            stroke = Color.parseColor("#33FFFFFF");
        }
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setStroke(dp(1), stroke);
        drawable.setCornerRadius(dp(7));
        return drawable;
    }

    private void updateSeatSummary() {
        List<String> selected = getSelectedSeatCodes();
        int total = getSeatTotal();
        if (selected.isEmpty()) {
            txtSeatPriceDetail.setText("Bạn chưa chọn ghế nào");
        } else {
            txtSeatPriceDetail.setText("Ghế: " + String.join(", ", selected) + "\nTạm tính: " + formatVnd(total));
        }
    }

    private void continueToPayment() {
        ArrayList<String> selected = new ArrayList<>(getSelectedSeatCodes());
        if (selected.isEmpty()) {
            toast("Vui lòng chọn ít nhất một ghế");
            return;
        }
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("movie", movie);
        intent.putExtra("movieTitle", movieTitle);
        intent.putExtra("cinema", cinema);
        intent.putExtra("showtime", showtime);
        intent.putExtra("showtimeId", showtimeId);
        intent.putStringArrayListExtra("selectedSeats", selected);
        intent.putExtra("totalPrice", getSeatTotal());
        startActivity(intent);
    }

    private List<String> getSelectedSeatCodes() {
        List<String> selected = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) if (seats[r][c].selected) selected.add(seats[r][c].code);
        return selected;
    }

    private int getSeatTotal() {
        int total = 0;
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) if (seats[r][c].selected) total += seats[r][c].price;
        return total;
    }

    private void startHoldTimer() {
        holdTimer = new CountDownTimer(5 * 60 * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvHoldCountdown.setText(String.format(Locale.getDefault(), "Thời gian giữ ghế: %02d:%02d", seconds / 60, seconds % 60));
            }

            @Override
            public void onFinish() {
                for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) seats[r][c].selected = false;
                setupSeats();
                updateSeatSummary();
                tvHoldCountdown.setText("Ghế đã hết thời gian giữ, vui lòng chọn lại");
                toast("Hết thời gian giữ ghế");
            }
        };
        holdTimer.start();
    }

    private String formatVnd(int amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + "đ";
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) if (value != null && !value.trim().isEmpty()) return value.trim();
        return "";
    }

    private String getRowLabel(int row) { return String.valueOf((char) ('A' + row)); }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }

    private static class Seat {
        final String code;
        String type;
        int price;
        boolean booked;
        boolean selected;
        Seat(String code, String type, int price, boolean booked) {
            this.code = code;
            this.type = type;
            this.price = price;
            this.booked = booked;
        }
    }
}
