package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Adapter.TicketHistoryAdapter;
import com.example.bookingcinema.Model.TicketHistory;
import com.example.bookingcinema.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private final List<TicketHistory> tickets = new ArrayList<>();
    private TicketHistoryAdapter historyAdapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = FirebaseFirestore.getInstance();
        RecyclerView recyclerHistory = findViewById(R.id.recyclerHistory);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new TicketHistoryAdapter(this, tickets);
        recyclerHistory.setAdapter(historyAdapter);
        setupBottomNav();
        loadHistory();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_history);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) startActivity(new Intent(this, MainActivity.class));
            else if (id == R.id.nav_history) return true;
            else if (id == R.id.nav_chat) startActivity(new Intent(this, ChatActivity.class));
            else if (id == R.id.nav_notifications) startActivity(new Intent(this, NotificationActivity.class));
            else if (id == R.id.nav_account) startActivity(new Intent(this, SettingsActivity.class));
            return true;
        });
    }

    private void loadHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            tickets.clear();
            tickets.addAll(fallbackHistory());
            historyAdapter.updateList(tickets);
            progressBar.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.GONE);
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        db.collection("tickets").whereEqualTo("userId", user.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    tickets.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) tickets.add(ticketFromDocument(doc));
                    Collections.sort(tickets, (a, b) -> Long.compare(b.getCreatedAtMillis(), a.getCreatedAtMillis()));
                    progressBar.setVisibility(View.GONE);
                    historyAdapter.updateList(tickets);
                    tvEmpty.setVisibility(tickets.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tickets.clear();
                    tickets.addAll(fallbackHistory());
                    historyAdapter.updateList(tickets);
                    tvEmpty.setVisibility(View.GONE);
                    toast("Không tải được lịch sử, đang dùng dữ liệu mẫu");
                });
    }

    private TicketHistory ticketFromDocument(DocumentSnapshot doc) {
        TicketHistory ticket = new TicketHistory();
        ticket.setTicketCode(firstNonEmpty(doc.getString("ticketCode"), doc.getId()));
        ticket.setMovieTitle(firstNonEmpty(doc.getString("movieTitle"), "Phim CINE-LUXE"));
        ticket.setCinema(firstNonEmpty(doc.getString("cinema"), "CINE-LUXE"));
        ticket.setShowtime(firstNonEmpty(doc.getString("showtime"), "Đang cập nhật"));
        ticket.setStatus(firstNonEmpty(doc.getString("status"), "Đã thanh toán"));
        ticket.setSeats(extractSeats(doc));
        Long total = doc.getLong("totalPrice");
        ticket.setTotalPrice(total == null ? 0 : total.intValue());
        Long created = doc.getLong("createdAtMillis");
        ticket.setCreatedAtMillis(created == null ? 0 : created);
        return ticket;
    }

    private String extractSeats(DocumentSnapshot doc) {
        String seatsText = doc.getString("seats");
        if (seatsText != null && !seatsText.trim().isEmpty()) return seatsText;
        Object raw = doc.get("selectedSeats");
        if (raw instanceof List<?>) {
            List<String> labels = new ArrayList<>();
            for (Object value : (List<?>) raw) if (value != null) labels.add(String.valueOf(value));
            return String.join(", ", labels);
        }
        return "Đang cập nhật";
    }

    private List<TicketHistory> fallbackHistory() {
        List<TicketHistory> list = new ArrayList<>();
        TicketHistory first = new TicketHistory("Dune: Phần Hai", "CINE-LUXE Landmark 81", "Hôm nay • 19:30 • H12", "D5, D6", 580000);
        first.setTicketCode("CINEVIP2026");
        first.setCreatedAtMillis(System.currentTimeMillis());
        list.add(first);
        return list;
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) if (value != null && !value.trim().isEmpty()) return value.trim();
        return "";
    }

    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
