package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bookingcinema.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PaymentActivity extends AppCompatActivity {

    private EditText edtDiscountCode, edtName, edtPhone;
    private Button btnApplyCode, btnPay;
    private ListView lvDiscountCodes, lvPaymentMethods;
    private ImageView ivQRCode;
    private TextView txtTotalPrice;

    private ArrayAdapter<String> discountAdapter;
    private List<String> discountCodes;

    private ArrayAdapter<String> paymentAdapter;
    private List<String> paymentMethods;

    private int originalTotalPrice = 0;
    private int finalPrice = 0;

    private ArrayList<String> selectedSeats;
    private String movieTitle, cinema, showtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        edtDiscountCode = findViewById(R.id.edtDiscountCode);
        btnApplyCode = findViewById(R.id.btnApplyCode);
        lvDiscountCodes = findViewById(R.id.lvDiscountCodes);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        btnPay = findViewById(R.id.btnPay);
        lvPaymentMethods = findViewById(R.id.lvPaymentMethods);
        ivQRCode = findViewById(R.id.ivQRCode);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);

        // Nhận dữ liệu từ ChooseSeatActivity
        Intent intent = getIntent();
        originalTotalPrice = intent.getIntExtra("totalPrice", 0);
        finalPrice = originalTotalPrice;
        selectedSeats = intent.getStringArrayListExtra("selectedSeats");
        movieTitle = intent.getStringExtra("movieTitle");
        cinema = intent.getStringExtra("cinema");
        showtime = intent.getStringExtra("showtime");

        txtTotalPrice.setText("Tổng tiền: " + originalTotalPrice + "₫");
        ivQRCode.setVisibility(View.GONE);
        lvDiscountCodes.setVisibility(View.GONE);

        // Mã giảm giá
        discountCodes = new ArrayList<>();
        discountCodes.add("GIAM20 - Giảm 20%");
        discountCodes.add("FREESHIP - Giảm 15000₫");
        discountCodes.add("SUMMER - Giảm 30000₫");

        discountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, discountCodes);
        lvDiscountCodes.setAdapter(discountAdapter);

        btnApplyCode.setOnClickListener(v -> {
            lvDiscountCodes.setVisibility(lvDiscountCodes.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });

        lvDiscountCodes.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = discountCodes.get(position);
            String selectedCode = selectedItem.split(" - ")[0];
            edtDiscountCode.setText(selectedCode);
            lvDiscountCodes.setVisibility(View.GONE);

            int discountAmount = 0;
            switch (selectedCode) {
                case "GIAM20": discountAmount = originalTotalPrice * 20 / 100; break;
                case "FREESHIP": discountAmount = 15000; break;
                case "SUMMER": discountAmount = 30000; break;
            }

            finalPrice = Math.max(originalTotalPrice - discountAmount, 0);
            txtTotalPrice.setText("Tổng tiền: " + finalPrice + "₫");
        });

        paymentMethods = new ArrayList<>();
        paymentMethods.add("Thanh toán tiền mặt");
        paymentMethods.add("Ví điện tử Momo");
        paymentMethods.add("Chuyển khoản ngân hàng");

        paymentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, paymentMethods);
        lvPaymentMethods.setAdapter(paymentAdapter);
        lvPaymentMethods.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvPaymentMethods.setItemChecked(0, true);

        lvPaymentMethods.setOnItemClickListener((parent, view, position, id) -> {
            String method = paymentMethods.get(position);
            if (method.equals("Ví điện tử Momo")) {
                ivQRCode.setVisibility(View.VISIBLE);
                ivQRCode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.qr));
            } else {
                ivQRCode.setVisibility(View.GONE);
            }
        });

        btnPay.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            int selectedPaymentPos = lvPaymentMethods.getCheckedItemPosition();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ họ tên và số điện thoại.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedPaymentPos == ListView.INVALID_POSITION) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán.", Toast.LENGTH_SHORT).show();
                return;
            }

            String paymentMethod = paymentMethods.get(selectedPaymentPos);
            String ticketCode = generateRandomTicketCode();

            // Gửi sang TicketSuccessActivity
            Intent successIntent = new Intent(this, TicketSuccessActivity.class);
            successIntent.putExtra("movieTitle", movieTitle);
            successIntent.putExtra("cinema", cinema);
            successIntent.putExtra("showtime", showtime);
            successIntent.putStringArrayListExtra("selectedSeats", selectedSeats);
            successIntent.putExtra("price", finalPrice);
            successIntent.putExtra("method", paymentMethod);
            successIntent.putExtra("ticketCode", ticketCode);
            startActivity(successIntent);
            finish();
        });
    }

    private String generateRandomTicketCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder("CGV");
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        code.append("VN");
        return code.toString();
    }
}
