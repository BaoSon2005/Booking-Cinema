package com.example.bookingcinema.UserScreen.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Adapter.NewsAdapter;
import com.example.bookingcinema.Model.News;
import com.example.bookingcinema.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NewsFragment extends Fragment {

    private final List<News> newsList = new ArrayList<>();
    private NewsAdapter newsAdapter;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        progressBar = view.findViewById(R.id.progressBar);
        RecyclerView recyclerNews = view.findViewById(R.id.recyclerNews);
        recyclerNews.setLayoutManager(new LinearLayoutManager(requireContext()));
        newsAdapter = new NewsAdapter(requireContext(), newsList);
        recyclerNews.setAdapter(newsAdapter);
        loadNewsCollection("notifications", true);
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

    private void toast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
