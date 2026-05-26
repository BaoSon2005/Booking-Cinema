package com.example.bookingcinema.AdminScreen.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.AdminScreen.Adapter.AlertAdapter;
import com.example.bookingcinema.AdminScreen.Model.AdminAlert;
import com.example.bookingcinema.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView tvRevenueToday, tvTicketsSold, tvComboCount, tvOccupancyRate;
    private final List<AdminAlert> alerts = new ArrayList<>();
    private AlertAdapter alertAdapter;
    private FirebaseFirestore db;
    private ListenerRegistration revenueRegistration, ticketsRegistration, ordersRegistration, seatsRegistration, alertsRegistration;
    private boolean viewDestroyed = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewDestroyed = false;
        db = FirebaseFirestore.getInstance();
        tvRevenueToday = view.findViewById(R.id.tvRevenueToday);
        tvTicketsSold = view.findViewById(R.id.tvTicketsSold);
        tvComboCount = view.findViewById(R.id.tvComboCount);
        tvOccupancyRate = view.findViewById(R.id.tvOccupancyRate);
        RecyclerView recyclerAlerts = view.findViewById(R.id.recyclerAlerts);
        recyclerAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        alertAdapter = new AlertAdapter(requireContext(), alerts);
        recyclerAlerts.setAdapter(alertAdapter);
        bindRealtimeStatistics();
    }

    @Override
    public void onDestroyView() {
        viewDestroyed = true; // FIX: Chặn callback Firestore cập nhật view sau khi Fragment bị tháo.
        removeListeners();
        super.onDestroyView();
    }

    private void bindRealtimeStatistics() {
        try {
            listenRevenueToday();
            listenTicketsSold();
            listenFoodOrders();
            listenOccupancyRate();
            listenAlerts();
        } catch (Exception e) {
            toast("Không thể khởi tạo bảng điều khiển");
        }
    }

    private void listenRevenueToday() {
        long start = startOfTodayMillis();
        long end = start + 24L * 60L * 60L * 1000L;
        revenueRegistration = db.collection("HoaDon")
                .whereGreaterThanOrEqualTo("createdAtMillis", start)
                .whereLessThan("createdAtMillis", end)
                .addSnapshotListener((snapshot, error) -> {
                    if (!isViewAlive()) return; // FIX: Tránh UI leak khi callback về trễ.
                    if (error != null || snapshot == null) {
                        tvRevenueToday.setText(formatVnd(0));
                        return;
                    }
                    long revenue = 0;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Long total = firstLong(doc, "totalPrice", "tongTien", "thanhTien");
                        if (total != null) revenue += total;
                    }
                    tvRevenueToday.setText(formatVnd(revenue));
                });
    }

    private void listenTicketsSold() {
        ticketsRegistration = db.collection("Tickets")
                .addSnapshotListener((snapshot, error) -> {
                    if (!isViewAlive()) return;
                    if (error != null || snapshot == null) {
                        listenHoaDonTicketFallback();
                        return;
                    }
                    tvTicketsSold.setText("Vé đã bán\n" + snapshot.size());
                });
    }

    private void listenHoaDonTicketFallback() {
        db.collection("HoaDon").get()
                .addOnSuccessListener(snapshot -> {
                    if (!isViewAlive()) return;
                    tvTicketsSold.setText("Vé đã bán\n" + snapshot.size());
                })
                .addOnFailureListener(e -> {
                    if (!isViewAlive()) return;
                    tvTicketsSold.setText("Vé đã bán\n0");
                });
    }

    private void listenFoodOrders() {
        ordersRegistration = db.collection("Orders")
                .addSnapshotListener((snapshot, error) -> {
                    if (!isViewAlive()) return;
                    if (error != null || snapshot == null) {
                        tvComboCount.setText("Combo F&B\n0");
                        return;
                    }
                    int active = 0;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String status = firstText(doc, "status", "trangThai");
                        if (!"Đã giao".equalsIgnoreCase(status)) active++;
                    }
                    tvComboCount.setText("Combo F&B\n" + active);
                });
    }

    private void listenOccupancyRate() {
        seatsRegistration = db.collection("seats")
                .addSnapshotListener((snapshot, error) -> {
                    if (!isViewAlive()) return;
                    if (error != null || snapshot == null || snapshot.isEmpty()) {
                        tvOccupancyRate.setText("Tỷ lệ lấp đầy\n0%");
                        return;
                    }
                    int total = snapshot.size();
                    int booked = 0;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String status = firstText(doc, "status", "seatStatus", "trangThai");
                        Boolean isBooked = doc.getBoolean("booked");
                        if (Boolean.TRUE.equals(isBooked)
                                || "booked".equalsIgnoreCase(status)
                                || "sold".equalsIgnoreCase(status)
                                || "Đã bán".equalsIgnoreCase(status)) {
                            booked++;
                        }
                    }
                    int rate = total == 0 ? 0 : Math.round(booked * 100f / total);
                    tvOccupancyRate.setText("Tỷ lệ lấp đầy\n" + rate + "%");
                });
    }

    private void listenAlerts() {
        alertsRegistration = db.collection("Alerts")
                .addSnapshotListener((snapshot, error) -> {
                    if (!isViewAlive()) return;
                    alerts.clear();
                    if (error == null && snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            AdminAlert alert = doc.toObject(AdminAlert.class);
                            if (alert != null) {
                                alert.setId(doc.getId());
                                alerts.add(alert);
                            }
                        }
                    }
                    if (alerts.isEmpty()) alerts.addAll(fallbackAlerts());
                    alertAdapter.refresh();
                });
    }

    private List<AdminAlert> fallbackAlerts() {
        List<AdminAlert> list = new ArrayList<>();
        AdminAlert room = new AdminAlert();
        room.setTitle("Cảnh báo: Phòng 02 sắp hết phim");
        room.setMessage("Vui lòng chuẩn bị vệ sinh phòng chiếu trước suất kế tiếp.");
        room.setSeverity("critical");
        list.add(room);
        AdminAlert stock = new AdminAlert();
        stock.setTitle("Cảnh báo: Kho bắp sắp hết ly giấy");
        stock.setMessage("Tồn kho hiện tại dưới 50 ly. Yêu cầu nhập thêm tại kho tổng.");
        stock.setSeverity("warning");
        list.add(stock);
        return list;
    }

    private void removeListeners() {
        if (revenueRegistration != null) revenueRegistration.remove();
        if (ticketsRegistration != null) ticketsRegistration.remove();
        if (ordersRegistration != null) ordersRegistration.remove();
        if (seatsRegistration != null) seatsRegistration.remove();
        if (alertsRegistration != null) alertsRegistration.remove();
        revenueRegistration = null;
        ticketsRegistration = null;
        ordersRegistration = null;
        seatsRegistration = null;
        alertsRegistration = null;
    }

    private long startOfTodayMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private Long firstLong(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            Long value = doc.getLong(key);
            if (value != null) return value;
        }
        return null;
    }

    private String firstText(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            String value = doc.getString(key);
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return "";
    }

    private String formatVnd(long amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount) + " đ";
    }

    private void toast(String message) {
        if (!isViewAlive()) return;
        Toast.makeText(requireContext().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isViewAlive() {
        return !viewDestroyed && isAdded() && getView() != null;
    }
}
