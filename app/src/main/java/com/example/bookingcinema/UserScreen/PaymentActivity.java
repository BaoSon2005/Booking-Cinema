package com.example.bookingcinema.UserScreen;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Adapter.FoodAdapter;
import com.example.bookingcinema.Model.FoodItem;
import com.example.bookingcinema.Model.Movie;
import com.example.bookingcinema.R;
import com.example.bookingcinema.Util.QrCodeGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.WriterException;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class PaymentActivity extends AppCompatActivity {

    private EditText edtDiscountCode, edtName, edtPhone;
    private ListView lvDiscountCodes, lvPaymentMethods;
    private ImageView ivQRCode;
    private TextView txtTotalPrice, txtTicketPrice, txtFoodPrice, tvSelectedSeats, tvSummary;
    private Button btnPickupTime, btnPay;
    private final List<FoodItem> foods = new ArrayList<>();
    private final List<Voucher> vouchers = new ArrayList<>();
    private ArrayList<String> selectedSeats;
    private Movie movie;
    private String movieTitle, cinema, showtime, showtimeId;
    private String pickupTime = "Nhận tại quầy trước giờ chiếu 15 phút";
    private int ticketPrice, foodPrice, discount;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FoodAdapter foodAdapter;
    private boolean destroyed = false;
    private boolean paymentCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        bindViews();
        readIntent();
        renderSummary();
        setupFoodList();
        setupVouchers();
        setupPaymentMethods();
        prefillCustomer();
        loadFoodsFromFirestore();
        loadVouchersFromFirestore();
        updateTotals();
    }

    @Override
    protected void onDestroy() {
        destroyed = true; // FIX: Ngăn callback Firebase cập nhật UI sau khi PaymentActivity đóng.
        super.onDestroy();
    }

    private void bindViews() {
        edtDiscountCode = findViewById(R.id.edtDiscountCode);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        lvDiscountCodes = findViewById(R.id.lvDiscountCodes);
        lvPaymentMethods = findViewById(R.id.lvPaymentMethods);
        ivQRCode = findViewById(R.id.ivQRCode);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        txtTicketPrice = findViewById(R.id.txtTicketPrice);
        txtFoodPrice = findViewById(R.id.txtFoodPrice);
        tvSelectedSeats = findViewById(R.id.tvSelectedSeats);
        tvSummary = findViewById(R.id.tvSummary);
        btnPickupTime = findViewById(R.id.btnPickupTime);
        btnPay = findViewById(R.id.btnPay);
        Button btnApplyCode = findViewById(R.id.btnApplyCode);
        btnPickupTime.setOnClickListener(v -> openPickupTimePicker());
        btnApplyCode.setOnClickListener(v -> toggleVouchers());
        btnPay.setOnClickListener(v -> confirmPayment());
    }

    private void readIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            intent = new Intent(); // FIX: Chống NPE khi Activity bị khởi tạo thiếu Intent.
        }
        movie = (Movie) intent.getSerializableExtra("movie");
        movieTitle = firstNonEmpty(intent.getStringExtra("movieTitle"), movie == null ? "" : movie.getTitle(), "Phim CINE-LUXE");
        cinema = firstNonEmpty(intent.getStringExtra("cinema"), "CINE-LUXE Landmark 81");
        showtime = firstNonEmpty(intent.getStringExtra("showtime"), "Hôm nay • 19:30 • H12");
        showtimeId = firstNonEmpty(intent.getStringExtra("showtimeId"), "fallback");
        selectedSeats = intent.getStringArrayListExtra("selectedSeats");
        if (selectedSeats == null) selectedSeats = new ArrayList<>();
        ticketPrice = intent.getIntExtra("totalPrice", 0);
    }

    private void renderSummary() {
        tvSummary.setText(movieTitle + "\n" + cinema + "\n" + showtime);
        tvSelectedSeats.setText("Ghế đã chọn: " + (selectedSeats.isEmpty() ? "Chưa có" : String.join(", ", selectedSeats)));
        txtTicketPrice.setText("Tiền vé: " + formatVnd(ticketPrice));
        btnPickupTime.setText(pickupTime);
    }

    private void setupFoodList() {
        RecyclerView recyclerFood = findViewById(R.id.recyclerFood);
        recyclerFood.setLayoutManager(new LinearLayoutManager(this));
        recyclerFood.setNestedScrollingEnabled(false);
        foodAdapter = new FoodAdapter(this, foods, () -> {
            calculateFoodTotal();
            updateTotals();
        });
        recyclerFood.setAdapter(foodAdapter);
    }

    private void setupVouchers() {
        lvDiscountCodes.setAdapter(voucherAdapter(new ArrayList<>()));
        lvDiscountCodes.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < vouchers.size()) {
                Voucher voucher = vouchers.get(position);
                edtDiscountCode.setText(voucher.code);
                applyVoucher(voucher);
                lvDiscountCodes.setVisibility(View.GONE);
            }
        });
    }

    private void setupPaymentMethods() {
        List<String> methods = new ArrayList<>();
        methods.add("Thẻ quốc tế / ATM nội địa");
        methods.add("Ví MoMo");
        methods.add("Chuyển khoản QR");
        lvPaymentMethods.setAdapter(darkSingleChoiceAdapter(methods));
        lvPaymentMethods.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvPaymentMethods.setItemChecked(0, true);
        lvPaymentMethods.setOnItemClickListener((parent, view, position, id) -> updatePaymentQr(methods.get(position)));
    }

    private void prefillCustomer() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        edtName.setText(firstNonEmpty(user.getDisplayName(), user.getEmail(), ""));
        edtPhone.setText(firstNonEmpty(user.getPhoneNumber(), ""));
    }

    private void loadFoodsFromFirestore() {
        try {
            db.collection("foods").get()
                .addOnSuccessListener(snapshot -> {
                    if (!isActivityAlive()) return;
                    foods.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        FoodItem food = doc.toObject(FoodItem.class);
                        if (food != null) {
                            food.setId(doc.getId());
                            foods.add(food);
                        }
                    }
                    if (foods.isEmpty()) foods.addAll(fallbackFoods());
                    foodAdapter.notifyDataSetChanged();
                    calculateFoodTotal();
                    updateTotals();
                })
                .addOnFailureListener(e -> {
                    if (!isActivityAlive()) return;
                    foods.clear();
                    foods.addAll(fallbackFoods());
                    foodAdapter.notifyDataSetChanged();
                    calculateFoodTotal();
                    updateTotals();
                    toast("Không tải được bắp nước, đang dùng combo mẫu");
                });
        } catch (Exception e) {
            if (!isActivityAlive()) return;
            foods.clear();
            foods.addAll(fallbackFoods());
            foodAdapter.notifyDataSetChanged();
            calculateFoodTotal();
            updateTotals();
            toast("Không tải được bắp nước, đang dùng combo mẫu");
        }
    }

    private void loadVouchersFromFirestore() {
        try {
            db.collection("vouchers").get()
                .addOnSuccessListener(snapshot -> {
                    if (!isActivityAlive()) return;
                    vouchers.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String code = firstNonEmpty(doc.getString("code"), doc.getId()).toUpperCase(Locale.ROOT);
                        String title = firstNonEmpty(doc.getString("title"), doc.getString("name"), "Ưu đãi CINE-LUXE");
                        Long percent = doc.getLong("discountPercent");
                        Long amount = doc.getLong("discountAmount");
                        String expiresAt = firstNonEmpty(doc.getString("expiresAt"), "Còn hiệu lực");
                        vouchers.add(new Voucher(code, title, percent == null ? 0 : percent.intValue(), amount == null ? 0 : amount.intValue(), expiresAt));
                    }
                    if (vouchers.isEmpty()) vouchers.addAll(fallbackVouchers());
                    refreshVoucherList();
                })
                .addOnFailureListener(e -> {
                    if (!isActivityAlive()) return;
                    vouchers.clear();
                    vouchers.addAll(fallbackVouchers());
                    refreshVoucherList();
                    toast("Không tải được voucher, đang dùng ưu đãi mẫu");
                });
        } catch (Exception e) {
            if (!isActivityAlive()) return;
            vouchers.clear();
            vouchers.addAll(fallbackVouchers());
            refreshVoucherList();
            toast("Không tải được voucher, đang dùng ưu đãi mẫu");
        }
    }

    private void refreshVoucherList() {
        List<String> labels = new ArrayList<>();
        for (Voucher voucher : vouchers) labels.add(voucher.label());
        lvDiscountCodes.setAdapter(voucherAdapter(labels));
    }

    private void toggleVouchers() {
        if (lvDiscountCodes.getVisibility() == View.VISIBLE) {
            lvDiscountCodes.setVisibility(View.GONE);
            String code = edtDiscountCode.getText().toString().trim().toUpperCase(Locale.ROOT);
            if (!code.isEmpty()) {
                Voucher voucher = findVoucher(code);
                if (voucher == null) {
                    discount = 0;
                    toast("Voucher không hợp lệ hoặc đã hết hạn");
                } else {
                    applyVoucher(voucher);
                }
            }
        } else {
            lvDiscountCodes.setVisibility(View.VISIBLE);
        }
        updateTotals();
    }

    private void applyVoucher(Voucher voucher) {
        int subtotal = ticketPrice + foodPrice;
        discount = voucher.discountPercent > 0 ? subtotal * voucher.discountPercent / 100 : voucher.discountAmount;
        discount = Math.min(discount, subtotal);
        toast("Đã áp dụng " + voucher.code);
        updateTotals();
    }

    private Voucher findVoucher(String code) {
        for (Voucher voucher : vouchers) if (voucher.code.equalsIgnoreCase(code)) return voucher;
        return null;
    }

    private void calculateFoodTotal() {
        foodPrice = 0;
        for (FoodItem food : foods) foodPrice += food.getLineTotal();
        txtFoodPrice.setText("Combo: " + formatVnd(foodPrice));
    }

    private void updateTotals() {
        txtTotalPrice.setText("Tổng thanh toán: " + formatVnd(getFinalPrice()));
    }

    private void openPickupTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            pickupTime = String.format(Locale.getDefault(), "Nhận combo lúc %02d:%02d", hourOfDay, minute);
            btnPickupTime.setText(pickupTime);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void updatePaymentQr(String method) {
        if (method.contains("MoMo") || method.contains("QR")) {
            try {
                Bitmap bitmap = QrCodeGenerator.create("CINE-LUXE|THANH_TOAN|" + movieTitle + "|" + getFinalPrice(), 512);
                ivQRCode.setImageBitmap(bitmap);
                ivQRCode.setVisibility(View.VISIBLE);
            } catch (WriterException e) {
                ivQRCode.setVisibility(View.GONE);
                toast("Không tạo được QR thanh toán");
            }
        } else {
            ivQRCode.setVisibility(View.GONE);
        }
    }

    private int getFinalPrice() {
        return Math.max(ticketPrice + foodPrice - discount, 0);
    }

    private void confirmPayment() {
        if (!isActivityAlive() || paymentCompleted) return; // FIX: Chống double click gây tạo nhiều hóa đơn.
        String name = edtName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        int checkedPayment = lvPaymentMethods.getCheckedItemPosition();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            toast("Vui lòng nhập họ tên và số điện thoại");
            return;
        }
        if (checkedPayment == ListView.INVALID_POSITION) {
            toast("Vui lòng chọn phương thức thanh toán");
            return;
        }

        btnPay.setEnabled(false);
        btnPay.setText("ĐANG XỬ LÝ");

        String paymentMethod = String.valueOf(lvPaymentMethods.getItemAtPosition(checkedPayment));
        String ticketId = generateTicketCode();
        int finalPrice = getFinalPrice();
        FirebaseUser user = auth.getCurrentUser();
        String uid = user == null ? "guest" : user.getUid();
        String seatText = String.join(", ", selectedSeats);

        Map<String, Object> invoice = new HashMap<>();
        invoice.put("idHoaDon", ticketId);
        invoice.put("ticketCode", ticketId);
        invoice.put("userId", uid);
        invoice.put("customerName", name);
        invoice.put("phone", phone);
        invoice.put("movieTitle", movieTitle);
        invoice.put("tenPhim", movieTitle);
        invoice.put("cinema", cinema);
        invoice.put("rap", cinema);
        invoice.put("showtime", showtime);
        invoice.put("suatChieu", showtime);
        invoice.put("showtimeId", showtimeId);
        invoice.put("selectedSeats", selectedSeats);
        invoice.put("seats", seatText);
        invoice.put("ghe", seatText);
        invoice.put("ticketPrice", ticketPrice);
        invoice.put("foodPrice", foodPrice);
        invoice.put("discount", discount);
        invoice.put("totalPrice", finalPrice);
        invoice.put("tongTien", finalPrice);
        invoice.put("paymentMethod", paymentMethod);
        invoice.put("pickupTime", pickupTime);
        invoice.put("status", "Đã thanh toán");
        invoice.put("trangThai", "Đã thanh toán");
        invoice.put("createdAt", FieldValue.serverTimestamp());
        invoice.put("createdAtMillis", System.currentTimeMillis());
        invoice.put("qrPayload", "CINE-LUXE|" + ticketId + "|" + movieTitle + "|" + seatText);

        db.collection("HoaDon").document(ticketId).set(invoice)
                .addOnSuccessListener(unused -> {
                    paymentCompleted = true;
                    markSeatsAsBooked();
                    rewardPoints(uid, finalPrice);
                    mirrorTicketHistory(ticketId, invoice);
                    sendAdminTicketNotification(ticketId, invoice);
                    if (!isActivityAlive()) return; // FIX: Backend vẫn đồng bộ, nhưng không mở màn hình nếu Activity đã đóng.
                    Intent intent = new Intent(PaymentActivity.this, XacNhanThanhToanActivity.class);
                    intent.putExtra("id_hoa_don", ticketId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // FIX: Không để Payment bị mở chồng khi quay lại từ vé.
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (!isActivityAlive()) return;
                    btnPay.setEnabled(true);
                    btnPay.setText("XÁC NHẬN");
                    toast("Không lưu được hóa đơn: " + safeMessage(e));
                });
    }

    private void mirrorTicketHistory(String ticketId, Map<String, Object> invoice) {
        db.collection("tickets").document(ticketId).set(invoice, SetOptions.merge());
        db.collection("Tickets").document(ticketId).set(invoice, SetOptions.merge());
    }

    private void sendAdminTicketNotification(String ticketId, Map<String, Object> invoice) {
        try {
            String customerName = firstNonEmpty(asText(invoice.get("customerName")), "Khách CINE-LUXE");
            String title = "Có đơn đặt vé mới!";
            String message = customerName
                    + " vừa đặt "
                    + Math.max(1, selectedSeats.size())
                    + " vé phim "
                    + firstNonEmpty(asText(invoice.get("movieTitle")), movieTitle, "Phim CINE-LUXE")
                    + ". Ghế: "
                    + firstNonEmpty(asText(invoice.get("seats")), "đang cập nhật")
                    + ".";

            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("message", message);
            notification.put("timestamp", FieldValue.serverTimestamp());
            notification.put("createdAtMillis", System.currentTimeMillis());
            notification.put("isRead", false);
            notification.put("type", "new_ticket");
            notification.put("ticketId", ticketId);
            notification.put("customerName", customerName);
            notification.put("movieTitle", firstNonEmpty(asText(invoice.get("movieTitle")), movieTitle));
            notification.put("seats", firstNonEmpty(asText(invoice.get("seats")), ""));
            notification.put("totalPrice", invoice.get("totalPrice"));

            db.collection("AdminNotifications")
                    .document(ticketId)
                    .set(notification, SetOptions.merge())
                    .addOnFailureListener(e -> {
                        if (!isActivityAlive()) return;
                        toast("Đã lưu vé nhưng chưa gửi được thông báo cho quản trị: " + safeMessage(e));
                    });
        } catch (Exception e) {
            toast("Không thể tạo thông báo quản trị: " + safeMessage(e));
        }
    }

    private void markSeatsAsBooked() {
        for (String seatCode : selectedSeats) {
            Map<String, Object> data = new HashMap<>();
            data.put("showtimeId", showtimeId);
            data.put("seatCode", seatCode);
            data.put("code", seatCode);
            data.put("booked", true);
            data.put("status", "booked");
            db.collection("seats").document(showtimeId + "_" + seatCode).set(data, SetOptions.merge());
        }
    }

    private void rewardPoints(String uid, int finalPrice) {
        if (!"guest".equals(uid)) {
            db.collection("users").document(uid).update("vipPoint", FieldValue.increment(Math.max(1, finalPrice / 10000)));
        }
    }

    private ArrayAdapter<String> darkSingleChoiceAdapter(List<String> items) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, items) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(getColor(R.color.cinema_text));
                view.setBackgroundColor(getColor(R.color.cinema_panel));
                return view;
            }
        };
    }

    private ArrayAdapter<String> voucherAdapter(List<String> items) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(getColor(R.color.cinema_text));
                view.setBackgroundColor(getColor(R.color.cinema_panel));
                view.setPadding(18, 14, 18, 14);
                return view;
            }
        };
    }

    private List<FoodItem> fallbackFoods() {
        List<FoodItem> list = new ArrayList<>();
        list.add(new FoodItem("combo-premium", "Combo Bắp Premium", "Bắp caramel lớn, 2 nước ngọt và quà phim", 189000, R.drawable.logo));
        list.add(new FoodItem("combo-couple", "Combo Đôi", "Bắp phô mai vừa và 2 nước ngọt", 129000, R.drawable.logo));
        list.add(new FoodItem("nachos", "Nachos phô mai", "Snack giòn dùng kèm sốt phô mai nóng", 79000, R.drawable.logo));
        return list;
    }

    private List<Voucher> fallbackVouchers() {
        List<Voucher> list = new ArrayList<>();
        list.add(new Voucher("LUXE20", "Giảm 20% cho vé trong tuần", 20, 0, "31/12/2026"));
        list.add(new Voucher("BAPNUOC50", "Giảm 50.000đ combo bắp nước", 0, 50000, "30/06/2026"));
        return list;
    }

    private String generateTicketCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder("CINE");
        for (int i = 0; i < 8; i++) code.append(chars.charAt(random.nextInt(chars.length())));
        return code.toString();
    }

    private String formatVnd(int amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + "đ";
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) if (value != null && !value.trim().isEmpty()) return value.trim();
        return "";
    }

    private String asText(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String safeMessage(Exception e) {
        return e.getMessage() == null ? "Lỗi không xác định" : e.getMessage();
    }

    private void toast(String message) {
        if (!isActivityAlive()) return;
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isActivityAlive() {
        return !destroyed && !isFinishing() && !isDestroyed();
    }

    private static class Voucher {
        final String code, title, expiresAt;
        final int discountPercent, discountAmount;

        Voucher(String code, String title, int discountPercent, int discountAmount, String expiresAt) {
            this.code = code;
            this.title = title;
            this.discountPercent = discountPercent;
            this.discountAmount = discountAmount;
            this.expiresAt = expiresAt;
        }

        String label() {
            String value = discountPercent > 0
                    ? "Giảm " + discountPercent + "%"
                    : "Giảm " + NumberFormat.getInstance(new Locale("vi", "VN")).format(discountAmount) + "đ";
            return code + " - " + title + " • " + value + " • HSD " + expiresAt;
        }
    }
}
