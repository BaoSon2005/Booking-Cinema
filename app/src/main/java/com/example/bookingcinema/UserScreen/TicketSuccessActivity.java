package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.R;
import com.example.bookingcinema.Util.QrCodeGenerator;
import com.google.zxing.WriterException;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TicketSuccessActivity extends AppCompatActivity {

    private TextView tvMovieTitle, tvCinema, tvShowtime, tvSeat, tvTotal, tvPaymentMethod, tvTicketCode;
    private ImageView imgTicketQr;

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
        imgTicketQr = findViewById(R.id.imgTicketQr);
        Button btnConfirmTicket = findViewById(R.id.btnConfirmTicket);
        btnConfirmTicket.setOnClickListener(view -> {
            Intent intentHome = new Intent(this, MainActivity.class);
            intentHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentHome);
            finish();
        });

        renderTicket();
    }

    private void renderTicket() {
        Intent intent = getIntent();
        String movieTitle = safe(intent.getStringExtra("movieTitle"), "Phim CINE-LUXE");
        String cinema = safe(intent.getStringExtra("cinema"), "CINE-LUXE Landmark 81");
        String showtime = safe(intent.getStringExtra("showtime"), "Hôm nay • 19:30");
        ArrayList<String> seats = intent.getStringArrayListExtra("selectedSeats");
        int price = intent.getIntExtra("price", 0);
        String method = safe(intent.getStringExtra("method"), "Thẻ quốc tế / ATM nội địa");
        String ticketCode = safe(intent.getStringExtra("ticketCode"), "CINE000000");
        String qrPayload = safe(intent.getStringExtra("qrPayload"), "CINE-LUXE|" + ticketCode + "|" + movieTitle);

        tvMovieTitle.setText(movieTitle);
        tvTicketCode.setText("Mã vé: " + ticketCode);
        tvCinema.setText("Rạp: " + cinema);
        tvShowtime.setText("Suất: " + showtime);
        tvSeat.setText("Ghế: " + (seats == null || seats.isEmpty() ? "Chưa có" : String.join(", ", seats)));
        tvTotal.setText("Tổng tiền: " + formatVnd(price));
        tvPaymentMethod.setText("Thanh toán: " + method);

        try {
            Bitmap bitmap = QrCodeGenerator.create(qrPayload, 640);
            imgTicketQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Toast.makeText(this, "Không tạo được mã QR vé", Toast.LENGTH_SHORT).show();
        }
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String formatVnd(int amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + "đ";
    }
}
