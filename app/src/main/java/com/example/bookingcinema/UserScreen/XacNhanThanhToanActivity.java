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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import java.text.NumberFormat;
import java.util.Locale;

public class XacNhanThanhToanActivity extends AppCompatActivity {

    private TextView tvInvoiceId, tvMovieTitle, tvCinema, tvShowtime, tvSeats, tvTotal;
    private ImageView imgTicketQr;
    private String invoiceId;
    private boolean destroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xac_nhan_thanh_toan);

        Intent sourceIntent = getIntent();
        invoiceId = sourceIntent == null ? "" : sourceIntent.getStringExtra("id_hoa_don"); // FIX: Chống NPE khi thiếu Intent.
        bindViews();
        setupBackHome();
        loadInvoice();
    }

    @Override
    protected void onDestroy() {
        destroyed = true; // FIX: Không cập nhật vé điện tử sau khi Activity đã đóng.
        super.onDestroy();
    }

    private void bindViews() {
        tvInvoiceId = findViewById(R.id.tvInvoiceId);
        tvMovieTitle = findViewById(R.id.tvMovieTitle);
        tvCinema = findViewById(R.id.tvCinema);
        tvShowtime = findViewById(R.id.tvShowtime);
        tvSeats = findViewById(R.id.tvSeats);
        tvTotal = findViewById(R.id.tvTotal);
        imgTicketQr = findViewById(R.id.imgTicketQr);
    }

    private void setupBackHome() {
        Button btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadInvoice() {
        if (invoiceId == null || invoiceId.trim().isEmpty()) {
            invoiceId = "CINE000000";
            renderInvoice(invoiceId, "Phim CINE-LUXE", "CINE-LUXE", "Đang cập nhật", "Đang cập nhật", 0, "CINE-LUXE|" + invoiceId);
            return;
        }

        FirebaseFirestore.getInstance().collection("HoaDon").document(invoiceId).get()
                .addOnSuccessListener(doc -> {
                    if (!isActivityAlive()) return;
                    if (doc == null || !doc.exists()) {
                        toast("Không tìm thấy hóa đơn, đang hiển thị vé tạm");
                        renderInvoice(invoiceId, "Phim CINE-LUXE", "CINE-LUXE", "Đang cập nhật", "Đang cập nhật", 0, "CINE-LUXE|" + invoiceId);
                        return;
                    }
                    renderFromDocument(doc);
                })
                .addOnFailureListener(e -> {
                    if (!isActivityAlive()) return;
                    toast("Không tải được hóa đơn");
                    renderInvoice(invoiceId, "Phim CINE-LUXE", "CINE-LUXE", "Đang cập nhật", "Đang cập nhật", 0, "CINE-LUXE|" + invoiceId);
                });
    }

    private void renderFromDocument(DocumentSnapshot doc) {
        if (doc == null || !doc.exists()) return; // FIX: Chặn đọc field trên document rỗng.
        String movieTitle = firstNonEmpty(doc.getString("movieTitle"), doc.getString("tenPhim"), "Phim CINE-LUXE");
        String cinema = firstNonEmpty(doc.getString("cinema"), doc.getString("rap"), "CINE-LUXE");
        String showtime = firstNonEmpty(doc.getString("showtime"), doc.getString("suatChieu"), "Đang cập nhật");
        String seats = firstNonEmpty(doc.getString("seats"), doc.getString("ghe"), "Đang cập nhật");
        Long total = firstLong(doc, "totalPrice", "tongTien", "thanhTien");
        String payload = firstNonEmpty(doc.getString("qrPayload"), "CINE-LUXE|" + invoiceId + "|" + movieTitle + "|" + seats);
        renderInvoice(invoiceId, movieTitle, cinema, showtime, seats, total == null ? 0 : total.intValue(), payload);
    }

    private void renderInvoice(String id, String movieTitle, String cinema, String showtime, String seats, int total, String payload) {
        if (!isActivityAlive()) return;
        tvInvoiceId.setText("Mã vé: " + id);
        tvMovieTitle.setText(movieTitle);
        tvCinema.setText("Rạp: " + cinema);
        tvShowtime.setText("Suất chiếu: " + showtime);
        tvSeats.setText("Ghế: " + seats);
        tvTotal.setText("Tổng tiền: " + formatVnd(total));
        try {
            Bitmap bitmap = QrCodeGenerator.create(payload, 900);
            imgTicketQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            toast("Không tạo được mã QR vé");
        }
    }

    private Long firstLong(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            Long value = doc.getLong(key);
            if (value != null) return value;
        }
        return null;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) if (value != null && !value.trim().isEmpty()) return value.trim();
        return "";
    }

    private String formatVnd(int amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + "đ";
    }

    private void toast(String message) {
        if (!isActivityAlive()) return;
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isActivityAlive() {
        return !destroyed && !isFinishing() && !isDestroyed();
    }
}
