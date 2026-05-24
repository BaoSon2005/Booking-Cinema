package com.example.bookingcinema.UserScreen;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Adapter.TicketHistoryAdapter;
import com.example.bookingcinema.Model.TicketHistory;
import com.example.bookingcinema.R;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerHistory;
    private TicketHistoryAdapter historyAdapter;
    private List<TicketHistory> ticketHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Ánh xạ RecyclerView
        recyclerHistory = findViewById(R.id.recyclerHistory);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách và adapter
        ticketHistoryList = getFakeHistory(); // Có thể thay bằng dữ liệu thật
        historyAdapter = new TicketHistoryAdapter(this, ticketHistoryList);
        recyclerHistory.setAdapter(historyAdapter);
    }

    // 🔽 Hàm tạo dữ liệu mẫu giống CGV
    private List<TicketHistory> getFakeHistory() {
        List<TicketHistory> list = new ArrayList<>();

        list.add(new TicketHistory(
                "Inside Out 2",
                "CGV Vincom Đồng Khởi",
                "02/07/2025 - 16:00",
                "B5, B6",
                140000
        ));

        list.add(new TicketHistory(
                "The Batman",
                "CGV Aeon Mall Bình Tân",
                "01/07/2025 - 20:15",
                "D1",
                70000
        ));

        list.add(new TicketHistory(
                "Kung Fu Panda 4",
                "CGV Crescent Mall",
                "30/06/2025 - 13:00",
                "A1, A2, A3",
                210000
        ));

        return list;
    }
}
