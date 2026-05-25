package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Adapter.NewsAdapter;
import com.example.bookingcinema.Model.News;
import com.example.bookingcinema.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private NewsAdapter newsAdapter;
    private final List<News> newsList = new ArrayList<>();
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        db = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);
        RecyclerView recyclerNews = findViewById(R.id.recyclerNews);
        recyclerNews.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(this, newsList);
        recyclerNews.setAdapter(newsAdapter);
        setupBottomNav();
        loadNewsCollection("notifications", true);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_notifications);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) startActivity(new Intent(this, MainActivity.class));
            else if (id == R.id.nav_history) startActivity(new Intent(this, HistoryActivity.class));
            else if (id == R.id.nav_chat) startActivity(new Intent(this, ChatActivity.class));
            else if (id == R.id.nav_notifications) return true;
            else if (id == R.id.nav_account) startActivity(new Intent(this, SettingsActivity.class));
            return true;
        });
    }

    private void loadNewsCollection(String collection, boolean tryNewsFallback) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection(collection).get()
                .addOnSuccessListener(snapshot -> {
                    newsList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        News news = doc.toObject(News.class);
                        if (news != null) newsList.add(news);
                    }
                    if (newsList.isEmpty() && tryNewsFallback) {
                        loadNewsCollection("news", false);
                        return;
                    }
                    if (newsList.isEmpty()) newsList.addAll(fallbackNews());
                    progressBar.setVisibility(View.GONE);
                    newsAdapter.updateList(newsList);
                })
                .addOnFailureListener(e -> {
                    if (tryNewsFallback) {
                        loadNewsCollection("news", false);
                        return;
                    }
                    progressBar.setVisibility(View.GONE);
                    newsList.clear();
                    newsList.addAll(fallbackNews());
                    newsAdapter.updateList(newsList);
                    toast("Không tải được tin mới, đang dùng dữ liệu mẫu");
                });
    }

    private List<News> fallbackNews() {
        List<News> list = new ArrayList<>();
        News voucher = new News("Combo bắp nước giảm 50%", "Đặt trước combo trên ứng dụng và nhận tại quầy riêng trước giờ chiếu.", R.drawable.logo);
        voucher.setExpiresAt("30/06/2026");
        list.add(voucher);
        News premiere = new News("Suất chiếu sớm cuối tuần", "Các phòng Premium mở thêm suất tối cho phim bom tấn mới.", R.drawable.interstellar);
        premiere.setExpiresAt("Cập nhật mỗi ngày");
        list.add(premiere);
        return list;
    }

    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
