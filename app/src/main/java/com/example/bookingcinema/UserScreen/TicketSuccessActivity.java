package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.R;

import java.util.ArrayList;

public class TicketSuccessActivity extends AppCompatActivity {

    private TextView tvMovieTitle, tvCinema, tvShowtime, tvSeat, tvTotal, tvPaymentMethod, tvTicketCode;
    private Button btnConfirmTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_success);

        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvCinema = findViewById(R.id.tvCinema);
        tvShowtime = findViewById(R.id.tvShowtime);
        tvSeat = findViewById(R.id.tvSeat);
        tvTotal = findViewById(R.id.tvTotal);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvTicketCode = findViewById(R.id.tvTicketCode);
        btnConfirmTicket = findViewById(R.id.btnConfirmTicket);

        Intent intent = getIntent();
        String movieTitle = intent.getStringExtra("movieTitle");
        String cinema = intent.getStringExtra("cinema");
        String showtime = intent.getStringExtra("showtime");
        ArrayList<String> seats = intent.getStringArrayListExtra("selectedSeats");
        int price = intent.getIntExtra("price", 0);
        String method = intent.getStringExtra("method");
        String ticketCode = intent.getStringExtra("ticketCode");

        tvMovieTitle.setText("🎬 " + (movieTitle != null ? movieTitle : "(Không rõ)"));
        tvCinema.setText("Rạp: " + (cinema != null ? cinema : "(Không rõ)"));
        tvShowtime.setText("Suất: " + (showtime != null ? showtime : "(Không rõ)"));
        tvSeat.setText("Ghế: " + (seats != null && !seats.isEmpty() ? String.join(", ", seats) : "(Không rõ)"));
        tvTotal.setText("Tổng tiền: " + price + "₫");
        tvPaymentMethod.setText("Thanh toán: " + (method != null ? method : "(Không rõ)"));
        tvTicketCode.setText("Mã vé: " + (ticketCode != null ? ticketCode : "(N/A)"));

        btnConfirmTicket.setOnClickListener(view -> {
            Intent intentHome = new Intent(this, MainActivity.class);
            intentHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentHome);
            finish();
        });
    }
}
