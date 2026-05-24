package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.Model.Movie;
import com.example.bookingcinema.R;

import java.util.ArrayList;
import java.util.List;

public class ChooseSeatActivity extends AppCompatActivity {

    private GridLayout gridSeats;
    private Button btnConfirm;
    private TextView txtSeatPriceDetail;

    private static final int ROWS = 8;
    private static final int COLS = 12;

    private enum SeatStatus {
        AVAILABLE, VIP, COUPLE, DELUXE, SWEETBOX, BOOKED
    }

    private final int PRICE_STANDARD = 70000;
    private final int PRICE_VIP = 100000;
    private final int PRICE_COUPLE = 150000;
    private final int PRICE_DELUXE = 120000;
    private final int PRICE_SWEETBOX = 180000;

    private SeatStatus[][] seatStatuses = new SeatStatus[ROWS][COLS];
    private boolean[][] seatSelected = new boolean[ROWS][COLS];

    private String movieTitle, cinema, showtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_seat);

        gridSeats = findViewById(R.id.gridSeats);
        btnConfirm = findViewById(R.id.btnConfirm);
        txtSeatPriceDetail = findViewById(R.id.txtSeatPriceDetail);

        Intent intent = getIntent();
        Movie movie = (Movie) intent.getSerializableExtra("movie");
        cinema = intent.getStringExtra("cinema");
        showtime = intent.getStringExtra("showtime");
        movieTitle = movie != null ? movie.getTitle() : "(Không rõ)";

        initSeatStatuses();
        setupSeats();

        btnConfirm.setOnClickListener(v -> {
            List<String> selectedSeats = new ArrayList<>();
            int totalPrice = 0;

            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    if (seatSelected[r][c]) {
                        selectedSeats.add(getRowLabel(r) + (c + 1));
                        totalPrice += getSeatPrice(seatStatuses[r][c]);
                    }
                }
            }

            if (selectedSeats.isEmpty()) {
                Toast.makeText(this, "Bạn chưa chọn ghế nào", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent paymentIntent = new Intent(this, PaymentActivity.class);
            paymentIntent.putExtra("movieTitle", movieTitle);
            paymentIntent.putExtra("cinema", cinema);
            paymentIntent.putExtra("showtime", showtime);
            paymentIntent.putStringArrayListExtra("selectedSeats", new ArrayList<>(selectedSeats));
            paymentIntent.putExtra("totalPrice", totalPrice);
            startActivity(paymentIntent);
        });
    }

    private void initSeatStatuses() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (r == 0 || r == 1) {
                    seatStatuses[r][c] = SeatStatus.VIP;
                } else if ((r == 2 || r == 3) && (c % 3 == 0 || c % 3 == 1)) {
                    seatStatuses[r][c] = SeatStatus.COUPLE;
                } else if (r == 4) {
                    seatStatuses[r][c] = SeatStatus.DELUXE;
                } else if (r == 5 && (c >= 4 && c <= 7)) {
                    seatStatuses[r][c] = SeatStatus.SWEETBOX;
                } else if ((r + c) % 11 == 0) {
                    seatStatuses[r][c] = SeatStatus.BOOKED;
                } else {
                    seatStatuses[r][c] = SeatStatus.AVAILABLE;
                }
                seatSelected[r][c] = false;
            }
        }
    }

    private void setupSeats() {
        gridSeats.removeAllViews();
        int totalWidth = getResources().getDisplayMetrics().widthPixels;
        int seatSize = totalWidth / COLS - 16;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                final int row = r;
                final int col = c;

                Button seatBtn = new Button(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(r, 1), GridLayout.spec(c, 1));
                seatBtn.setLayoutParams(params);
                seatBtn.setWidth(seatSize);
                seatBtn.setHeight(seatSize);
                seatBtn.setText(getRowLabel(r) + (c + 1));
                seatBtn.setTextSize(10);
                seatBtn.setAllCaps(false);

                updateSeatButtonColor(seatBtn, seatStatuses[r][c], false);

                if (seatStatuses[r][c] == SeatStatus.BOOKED) {
                    seatBtn.setEnabled(false);
                }

                seatBtn.setOnClickListener(v -> {
                    seatSelected[row][col] = !seatSelected[row][col];
                    updateSeatButtonColor(seatBtn, seatStatuses[row][col], seatSelected[row][col]);
                    updateSeatPriceText();
                });

                gridSeats.addView(seatBtn);
            }
        }
    }

    private void updateSeatButtonColor(Button seatBtn, SeatStatus status, boolean selected) {
        if (selected) {
            seatBtn.setBackgroundColor(Color.parseColor("#4CAF50"));
            seatBtn.setTextColor(Color.WHITE);
            return;
        }
        switch (status) {
            case AVAILABLE: seatBtn.setBackgroundColor(Color.GRAY); break;
            case VIP: seatBtn.setBackgroundColor(Color.YELLOW); break;
            case COUPLE: seatBtn.setBackgroundColor(Color.MAGENTA); break;
            case DELUXE: seatBtn.setBackgroundColor(Color.parseColor("#8A2BE2")); break;
            case SWEETBOX: seatBtn.setBackgroundColor(Color.parseColor("#FF5722")); break;
            case BOOKED: seatBtn.setBackgroundColor(Color.parseColor("#9e6e6e")); break;
        }
        seatBtn.setTextColor(Color.BLACK);
    }

    private void updateSeatPriceText() {
        List<String> selectedSeats = new ArrayList<>();
        int totalPrice = 0;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (seatSelected[r][c]) {
                    selectedSeats.add(getRowLabel(r) + (c + 1));
                    totalPrice += getSeatPrice(seatStatuses[r][c]);
                }
            }
        }

        if (selectedSeats.isEmpty()) {
            txtSeatPriceDetail.setText("Bạn chưa chọn ghế nào");
        } else {
            txtSeatPriceDetail.setText("Ghế: " + String.join(", ", selectedSeats) + "\nTổng tiền: " + totalPrice + "₫");
        }
    }

    private int getSeatPrice(SeatStatus status) {
        switch (status) {
            case VIP: return PRICE_VIP;
            case COUPLE: return PRICE_COUPLE;
            case DELUXE: return PRICE_DELUXE;
            case SWEETBOX: return PRICE_SWEETBOX;
            default: return PRICE_STANDARD;
        }
    }

    private String getRowLabel(int row) {
        return String.valueOf((char) ('A' + row));
    }
}
