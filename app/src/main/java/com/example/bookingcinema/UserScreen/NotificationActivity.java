package com.example.bookingcinema.UserScreen;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookingcinema.Adapter.NewsAdapter;
import com.example.bookingcinema.Model.News;
import com.example.bookingcinema.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerNews;
    private NewsAdapter newsAdapter;
    private List<News> newsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);

        recyclerNews = findViewById(R.id.recyclerNews);
        recyclerNews.setLayoutManager(new LinearLayoutManager(this));

        newsList = new ArrayList<>();

        // 🔥 Danh sách thông báo phim mới như CGV App
        newsList.add(new News(
                "🎬 Avatar 3: Khởi chiếu sắp tới!",
                "🌌 Trở lại Pandora với công nghệ hình ảnh siêu thực. Đặt vé ngay để chọn ghế đẹp nhất!",
                R.drawable.avatar
        ));

        newsList.add(new News(
                "🐼 Kung Fu Panda 4 đã có suất chiếu!",
                "✨ Po và đồng đội mang đến cuộc phiêu lưu mới đầy hài hước và cảm xúc. Đặt vé liền tay!",
                R.drawable.k
        ));

        newsList.add(new News(
                "🦇 The Batman: Bóng tối trỗi dậy",
                "🔥 Gotham rơi vào hỗn loạn, Người Dơi trở lại với kẻ thù bí ẩn. Xem ngay tại rạp gần bạn!",
                R.drawable.batman
        ));

        newsList.add(new News(
                "💭 Inside Out 2 chính thức ra rạp!",
                "🎈 Những cảm xúc mới của Riley khiến hành trình trưởng thành trở nên kịch tính và cảm động. Đặt vé ngay!",
                R.drawable.inside
        ));

        newsAdapter = new NewsAdapter(this, newsList);
        recyclerNews.setAdapter(newsAdapter);
    }
}
