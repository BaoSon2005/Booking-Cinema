package com.example.bookingcinema.UserScreen.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Adapter.TicketHistoryAdapter;
import com.example.bookingcinema.Model.TicketHistory;
import com.example.bookingcinema.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryFragment extends Fragment {

    private final List<TicketHistory> tickets = new ArrayList<>();
    private TicketHistoryAdapter historyAdapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        RecyclerView recyclerHistory = view.findViewById(R.id.recyclerHistory);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyAdapter = new TicketHistoryAdapter(requireContext(), tickets);
        recyclerHistory.setAdapter(historyAdapter);
        loadHistory();
    }

    private void loadHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        progressBar.setVisibility(View.VISIBLE);
        if (user == null) {
            tickets.clear();
            tickets.addAll(fallbackHistory());
            finishLoad();
            return;
        }
        db.collection("HoaDon").whereEqualTo("userId", user.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    tickets.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) tickets.add(ticketFromDocument(doc));
                    Collections.sort(tickets, (a, b) -> Long.compare(b.getCreatedAtMillis(), a.getCreatedAtMillis()));
                    finishLoad();
                })
                .addOnFailureListener(e -> {
                    tickets.clear();
                    tickets.addAll(fallbackHistory());
                    finishLoad();
                    toast("Không tải được lịch sử, đang dùng dữ liệu mẫu");
                });
    }

    private void finishLoad() {
        progressBar.setVisibility(View.GONE);
        historyAdapter.updateList(tickets);
        tvEmpty.setVisibility(tickets.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private TicketHistory ticketFromDocument(DocumentSnapshot doc) {
        TicketHistory ticket = new TicketHistory();
        ticket.setTicketCode(firstNonEmpty(doc.getString("ticketCode"), doc.getString("idHoaDon"), doc.getId()));
        ticket.setMovieTitle(firstNonEmpty(doc.getString("movieTitle"), doc.getString("tenPhim"), "Phim CINE-LUXE"));
        ticket.setCinema(firstNonEmpty(doc.getString("cinema"), doc.getString("rap"), "CINE-LUXE"));
        ticket.setShowtime(firstNonEmpty(doc.getString("showtime"), doc.getString("suatChieu"), "Đang cập nhật"));
        ticket.setStatus(firstNonEmpty(doc.getString("status"), doc.getString("trangThai"), "Đã thanh toán"));
        ticket.setSeats(extractSeats(doc));
        Long total = firstLong(doc, "totalPrice", "tongTien", "thanhTien");
        ticket.setTotalPrice(total == null ? 0 : total.intValue());
        Long created = firstLong(doc, "createdAtMillis", "thoiGianTao");
        ticket.setCreatedAtMillis(created == null ? 0 : created);
        return ticket;
    }

    private String extractSeats(DocumentSnapshot doc) {
        String seatsText = firstNonEmpty(doc.getString("seats"), doc.getString("ghe"));
        if (!seatsText.isEmpty()) return seatsText;
        Object raw = doc.get("selectedSeats");
        if (raw instanceof List<?>) {
            List<String> labels = new ArrayList<>();
            for (Object value : (List<?>) raw) if (value != null) labels.add(String.valueOf(value));
            return String.join(", ", labels);
        }
        return "Đang cập nhật";
    }

    private Long firstLong(DocumentSnapshot doc, String... keys) {
        for (String key : keys) {
            Long value = doc.getLong(key);
            if (value != null) return value;
        }
        return null;
    }

    private List<TicketHistory> fallbackHistory() {
        List<TicketHistory> list = new ArrayList<>();
        TicketHistory first = new TicketHistory("Dune: Phần Hai", "CINE-LUXE Landmark 81", "Hôm nay • 19:30 • H12", "D5, D6", 580000);
        first.setTicketCode("CINEVIP2026");
        first.setStatus("Đã thanh toán");
        first.setCreatedAtMillis(System.currentTimeMillis());
        list.add(first);
        return list;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) if (value != null && !value.trim().isEmpty()) return value.trim();
        return "";
    }

    private void toast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
