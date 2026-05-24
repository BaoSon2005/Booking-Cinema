package com.example.bookingcinema.AdminScreen;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bookingcinema.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvTotalRevenue, tvTotalTickets, tvTotalMovies;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalTickets = findViewById(R.id.tvTotalTickets);
        tvTotalMovies = findViewById(R.id.tvTotalMovies);
        barChart = findViewById(R.id.barChart);

        displayStatistics();
        setupBarChart();
    }

    private void displayStatistics() {
        int totalRevenue = 120_000_000;
        int totalTickets = 780;
        int totalMovies = 12;

        tvTotalRevenue.setText("Tổng doanh thu: " + totalRevenue + " VNĐ");
        tvTotalTickets.setText("Tổng vé đã bán: " + totalTickets);
        tvTotalMovies.setText("Số phim đang chiếu: " + totalMovies);
    }

    private void setupBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 20));
        entries.add(new BarEntry(2, 45));
        entries.add(new BarEntry(3, 30));
        entries.add(new BarEntry(4, 55));
        entries.add(new BarEntry(5, 42));
        entries.add(new BarEntry(6, 60));

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu (triệu VNĐ)");
        dataSet.setColor(getResources().getColor(R.color.colorAccent));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.animateY(1000);

        Description desc = new Description();
        desc.setText("Doanh thu theo tháng");
        barChart.setDescription(desc);
    }
}
